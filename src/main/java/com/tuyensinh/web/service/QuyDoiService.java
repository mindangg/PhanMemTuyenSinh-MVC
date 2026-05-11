package com.tuyensinh.web.service;

import com.tuyensinh.web.entity.BangQuyDoi;
import com.tuyensinh.web.repository.BangQuyDoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Quy đổi điểm V-SAT → thang 10, ĐGNL → thang 30, TA chứng chỉ → điểm.
 * Thuật toán: nội suy tuyến tính trong khoảng [a, b] → [c, d] từ bảng xt_bangquydoi.
 */
@Service
public class QuyDoiService {

    @Autowired
    private BangQuyDoiRepository bangQuyDoiRepository;

    /**
     * Quy đổi điểm V-SAT (thang 150) về thang 10, theo từng môn.
     * @param maMon mã môn trong DB (TO, LI, HO, SI, VA, SU, DI, TI, KTPL)
     * @param diemVSAT điểm V-SAT thô
     */
    public double vsatToScale10(String maMon, double diemVSAT) {
        List<BangQuyDoi> rows = bangQuyDoiRepository.findByPhuongThucAndMonOrderByDiemAAsc("VSAT", maMon);
        return noiSuyTuyenTinh(diemVSAT, rows);
    }

    /**
     * Quy đổi điểm ĐGNL (thang 1200) về thang 30.
     */
    public double dgnlToScale30(double diemDgnl) {
        List<BangQuyDoi> rows = bangQuyDoiRepository.findByPhuongThuc("DGNL");
        return noiSuyTuyenTinh(diemDgnl, rows);
    }

    /**
     * Nội suy tuyến tính: tìm khoảng [a, b] chứa x, rồi map sang [c, d].
     * y = c + (x - a) * (d - c) / (b - a)
     */
    private double noiSuyTuyenTinh(double x, List<BangQuyDoi> rows) {
        if (rows == null || rows.isEmpty()) return x;

        for (BangQuyDoi row : rows) {
            double a = row.getDiemA() != null ? row.getDiemA() : 0;
            double b = row.getDiemB() != null ? row.getDiemB() : 0;
            double c = row.getDiemC() != null ? row.getDiemC() : 0;
            double d = row.getDiemD() != null ? row.getDiemD() : 0;

            if (x > a && x <= b) {
                if (b == a) return c;
                return c + (x - a) * (d - c) / (b - a);
            }
        }
        // Ngoài range: lấy đầu hoặc cuối
        BangQuyDoi first = rows.get(0);
        BangQuyDoi last = rows.get(rows.size() - 1);
        if (x <= (first.getDiemA() != null ? first.getDiemA() : 0)) {
            return first.getDiemC() != null ? first.getDiemC() : 0;
        }
        return last.getDiemD() != null ? last.getDiemD() : 0;
    }

    // ================================================================
    // Điểm cộng chứng chỉ tiếng Anh → điểm quy đổi hoặc điểm cộng
    // Nguồn: bangQUyDoiTA_2025.docx
    // ================================================================

    public enum MucChungChi { MUC1, MUC2, MUC3 }

    /** Trả về điểm môn Tiếng Anh quy đổi từ chứng chỉ (khi tổ hợp có TA). */
    public double taChungChiToMon(MucChungChi muc) {
        return DiemCongTaConstants.DIEM_QD_MON.getOrDefault(muc, 0.0);
    }

    /** Trả về điểm cộng TA vào THXT (thang 30) khi tổ hợp KHÔNG có môn TA. */
    public double taChungChiToDiemCongThpt(MucChungChi muc) {
        return DiemCongTaConstants.DIEM_CONG_THPT.getOrDefault(muc, 0.0);
    }

    /** Trả về điểm cộng TA cho ĐGNL (thang 1200) khi tổ hợp KHÔNG có môn TA. */
    public double taChungChiToDiemCongDgnl(MucChungChi muc) {
        return DiemCongTaConstants.DIEM_CONG_DGNL.getOrDefault(muc, 0.0);
    }
}
