package com.tuyensinh.web.service;

import com.tuyensinh.web.dto.DgnlForm;
import com.tuyensinh.web.dto.KetQuaTinhDiem;
import com.tuyensinh.web.dto.ThptVsatForm;
import com.tuyensinh.web.entity.Nganh;
import com.tuyensinh.web.entity.NganhToHop;
import com.tuyensinh.web.repository.NganhRepository;
import com.tuyensinh.web.repository.NganhToHopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TinhDiemXetTuyenService {

    private static final double DIEM_CONG_TOI_DA = 3.0;
    private static final double DIEM_XET_TUYEN_TOI_DA = 30.0;

    @Autowired private NganhRepository nganhRepository;
    @Autowired private NganhToHopRepository nganhToHopRepository;
    @Autowired private QuyDoiService quyDoiService;
    @Autowired private UuTienService uuTienService;
    @Autowired private DoLechToHopService doLechToHopService;

    // ========= ĐGNL =========

    @Transactional(readOnly = true)
    public KetQuaTinhDiem tinhDiemDgnl(DgnlForm form) {
        KetQuaTinhDiem result = new KetQuaTinhDiem();
        result.setPhuongThuc("ĐGNL");

        Nganh nganh = nganhRepository.findByMaNganh(form.getMaNganh()).orElse(null);
        result.setTenNganh(nganh != null ? nganh.getTenNganh() : form.getMaNganh());
        result.setMaNganh(form.getMaNganh());

        // 1. Quy đổi điểm ĐGNL (1200) → thang 30
        double dthxt = quyDoiService.dgnlToScale30(form.getDiemDgnl());
        result.setDiemToHop(form.getDiemDgnl());
        result.setDiemQuyDoi30(round2(dthxt));

        // 2. ĐGNL không cần quy đổi về tổ hợp gốc: ĐTHGXT = ĐTHXT
        double dthgxt = dthxt;

        // 3. Điểm cộng TA (nếu có)
        double dcTA = 0;
        if (form.getMucChungChiTA() != null) {
            dcTA = quyDoiService.taChungChiToDiemCongThpt(form.getMucChungChiTA());
        }
        // Điểm cộng tự bổ sung
        double dcBoSung = form.getDiemCongBoSung() != null ? form.getDiemCongBoSung() : 0;
        // Tổng ĐC, cap 3 điểm
        double dc = Math.min(DIEM_CONG_TOI_DA, dcTA + dcBoSung);
        result.setDiemCong(round2(dc));

        // 4. Điểm ưu tiên
        double dut = uuTienService.tinhDUT(dthgxt, dc, form.getDoiTuong(), form.getKhuVuc());
        result.setDiemUuTien(round2(dut));

        // 5. Điểm xét tuyển = ĐTHGXT + ĐC + ĐƯT, max 30
        double dxt = Math.min(DIEM_XET_TUYEN_TOI_DA, dthgxt + dc + dut);
        result.setDiemXetTuyen(round2(dxt));

        // 6. So sánh ngưỡng và điểm trúng tuyển
        if (nganh != null) {
            result.setDiemSan(nganh.getDiemSan());
            result.setDiemTrungTuyen(nganh.getDiemTrungTuyen());
            result.setDatNguong(nganh.getDiemSan() == null || dxt >= nganh.getDiemSan());
            result.setDatTrungTuyen(nganh.getDiemTrungTuyen() != null && dxt >= nganh.getDiemTrungTuyen());
        }
        return result;
    }

    // ========= THPT / VSAT =========

    @Transactional(readOnly = true)
    public List<KetQuaTinhDiem> tinhDiemThptVsat(ThptVsatForm form) {
        List<KetQuaTinhDiem> results = new ArrayList<>();
        boolean isVsat = "VSAT".equalsIgnoreCase(form.getPhuongThuc());

        Nganh nganh = nganhRepository.findByMaNganh(form.getMaNganh()).orElse(null);
        List<NganhToHop> toHopList = nganhToHopRepository.findByMaNganh(form.getMaNganh());

        // Quy đổi TA chứng chỉ thành điểm môn nếu có
        Double diemTaQD = null;
        if (form.getMucChungChiTA() != null) {
            diemTaQD = quyDoiService.taChungChiToMon(form.getMucChungChiTA());
        }

        // Build bảng điểm các môn đã quy đổi (VSAT → 10 hoặc giữ nguyên THPT)
        Map<String, Double> diemMon = new HashMap<>();
        Map<String, Double> raw = form.getDiemCacMon();
        if (raw != null) {
            for (Map.Entry<String, Double> e : raw.entrySet()) {
                String ma = e.getKey().toUpperCase();
                Double diem = e.getValue() != null ? e.getValue() : 0.0;
                if (isVsat) {
                    diem = quyDoiService.vsatToScale10(ma, diem);
                }
                diemMon.put(ma, diem);
            }
        }
        // Override TI nếu có chứng chỉ TA và môn TI
        if (diemTaQD != null) {
            diemMon.put("TI", diemTaQD);
        }

        for (NganhToHop nth : toHopList) {
            KetQuaTinhDiem r = new KetQuaTinhDiem();
            r.setPhuongThuc(form.getPhuongThuc());
            r.setMaNganh(form.getMaNganh());
            r.setTenNganh(nganh != null ? nganh.getTenNganh() : form.getMaNganh());
            r.setMaToHop(nth.getMaToHop());

            // Lấy điểm 3 môn trong tổ hợp, hệ số
            double d1 = getDiem(diemMon, nth.getThMon1());
            double d2 = getDiem(diemMon, nth.getThMon2());
            double d3 = getDiem(diemMon, nth.getThMon3());
            int w1 = nth.getHsMon1() != null ? nth.getHsMon1() : 1;
            int w2 = nth.getHsMon2() != null ? nth.getHsMon2() : 1;
            int w3 = nth.getHsMon3() != null ? nth.getHsMon3() : 1;
            int W = w1 + w2 + w3;

            // ĐTHXT = [(d1*w1 + d2*w2 + d3*w3) / W] * 3
            double diemTH = (d1 * w1 + d2 * w2 + d3 * w3);
            double dthxt = (diemTH / W) * 3.0;
            r.setDiemToHop(round2(diemTH / W * (isVsat ? 10.0 / 10.0 : 1.0)));
            r.setDiemQuyDoi30(round2(dthxt));

            // Quy đổi về tổ hợp gốc
            String toHopGoc = nganh != null ? nganh.getToHopGoc() : null;
            double dthgxt = doLechToHopService.quyDoiVeToHopGoc(toHopGoc, nth.getMaToHop(), dthxt);
            r.setDiemToHopGoc(round2(dthgxt));

            // Điểm cộng TA (nếu tổ hợp không có TA và có CC)
            double dcTA = 0;
            if (form.getMucChungChiTA() != null && diemTaQD == null) {
                // Tổ hợp không có môn TI → dùng điểm cộng
                boolean toHopCoTA = nth.getTi() != null && nth.getTi() > 0;
                if (!toHopCoTA) {
                    dcTA = quyDoiService.taChungChiToDiemCongThpt(form.getMucChungChiTA());
                }
            }
            double dcBoSung = form.getDiemCongBoSung() != null ? form.getDiemCongBoSung() : 0;
            double dc = Math.min(DIEM_CONG_TOI_DA, dcTA + dcBoSung);
            r.setDiemCong(round2(dc));

            // Điểm ưu tiên
            double dut = uuTienService.tinhDUT(dthgxt, dc, form.getDoiTuong(), form.getKhuVuc());
            r.setDiemUuTien(round2(dut));

            // Điểm xét tuyển
            double dxt = Math.min(DIEM_XET_TUYEN_TOI_DA, dthgxt + dc + dut);
            r.setDiemXetTuyen(round2(dxt));

            // So sánh ngưỡng
            if (nganh != null) {
                r.setDiemSan(nganh.getDiemSan());
                r.setDiemTrungTuyen(nganh.getDiemTrungTuyen());
                r.setDatNguong(nganh.getDiemSan() == null || dxt >= nganh.getDiemSan());
                r.setDatTrungTuyen(nganh.getDiemTrungTuyen() != null && dxt >= nganh.getDiemTrungTuyen());
            }
            results.add(r);
        }
        return results;
    }

    private double getDiem(Map<String, Double> map, String ma) {
        if (ma == null) return 0;
        return map.getOrDefault(ma.toUpperCase(), 0.0);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
