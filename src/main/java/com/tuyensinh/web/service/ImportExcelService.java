package com.tuyensinh.web.service;

import com.tuyensinh.web.entity.*;
import com.tuyensinh.web.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Service
public class ImportExcelService {

    @Autowired private NganhRepository nganhRepository;
    @Autowired private NganhToHopRepository nganhToHopRepository;
    @Autowired private ThiSinhRepository thiSinhRepository;
    @Autowired private DiemThiRepository diemThiRepository;
    @Autowired private NguyenVongRepository nguyenVongRepository;

    private static final Map<String, String> KV_MAP = Map.of(
            "1", "KV1", "2NT", "KV2-NT", "2", "KV2", "3", "KV3"
    );
    private static final Map<String, String> DT_MAP = Map.of(
            "1", "UT1", "2", "UT2", "3", "UT3", "4", "UT4",
            "5", "UT5", "6", "UT6", "7", "UT7"
    );

    // ──────────────────────────────────────────────
    // 1. Import xt_nganh từ Chi tieu 2025.xlsx
    // ──────────────────────────────────────────────
    @Transactional
    public String importChiTieu(MultipartFile file) {
        int them = 0, boQua = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            Set<String> existing = new HashSet<>(
                    nganhRepository.findAllByOrderByMaNganhAsc()
                            .stream().map(Nganh::getMaNganh).toList()
            );

            // Header ở row 1 (row 0 là tiêu đề lớn), data từ row 2
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String ma = fmt.formatCellValue(row.getCell(1)).trim();
                String ten = fmt.formatCellValue(row.getCell(2)).trim();
                if (ma.isEmpty() || !ma.matches("\\d+")) continue;

                if (existing.contains(ma)) { boQua++; continue; }

                Nganh n = new Nganh();
                n.setMaNganh(ma);
                n.setTenNganh(ten);
                n.setChiTieu(parseIntSafe(fmt.formatCellValue(row.getCell(3))));
                nganhRepository.save(n);
                existing.add(ma);
                them++;
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return String.format("Thêm mới: %d ngành. Bỏ qua (đã có): %d.", them, boQua);
    }

    // ──────────────────────────────────────────────
    // 2. Import ngưỡng đầu vào → cập nhật xt_nganh
    // ──────────────────────────────────────────────
    @Transactional
    public String importNguong(MultipartFile file) {
        int capNhat = 0, boQua = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            // Header ở row 0, data từ row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String ma = fmt.formatCellValue(row.getCell(1)).trim();
                if (ma.isEmpty() || !ma.matches("\\d+")) continue;

                Double diemSan = parseDoubleSafe(fmt.formatCellValue(row.getCell(3)));

                Optional<Nganh> opt = nganhRepository.findByMaNganh(ma);
                if (opt.isEmpty()) { boQua++; continue; }

                Nganh n = opt.get();
                n.setDiemSan(diemSan);
                nganhRepository.save(n);
                capNhat++;
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return String.format("Cập nhật ngưỡng: %d ngành. Bỏ qua (chưa có ngành): %d.", capNhat, boQua);
    }

    // ──────────────────────────────────────────────
    // 3. Import tohopmon.xlsx → xt_nganh_tohop
    //    + cập nhật n_tohopgoc trong xt_nganh
    // ──────────────────────────────────────────────
    @Transactional
    public String importToHop(MultipartFile file) {
        int them = 0, boQua = 0, capNhatGoc = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            Set<String> existingKeys = new HashSet<>(
                    nganhToHopRepository.findAll()
                            .stream().map(NganhToHop::getTbKeys).filter(Objects::nonNull).toList()
            );

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String maNganh    = fmt.formatCellValue(row.getCell(1)).trim();
                String maToHopRaw = fmt.formatCellValue(row.getCell(3)).trim(); // B03(TO-3,VA-3,SI-1)
                String tbKeys     = fmt.formatCellValue(row.getCell(4)).trim(); // 7140114_B03
                String maToHop    = fmt.formatCellValue(row.getCell(5)).trim(); // B03
                String goc        = fmt.formatCellValue(row.getCell(6)).trim(); // "Gốc" hoặc rỗng
                Double doLech     = parseDoubleSafe(fmt.formatCellValue(row.getCell(7)));

                if (maNganh.isEmpty() || maToHopRaw.isEmpty()) continue;
                if (existingKeys.contains(tbKeys)) { boQua++; continue; }

                NganhToHop nth = new NganhToHop();
                nth.setMaNganh(maNganh);
                nth.setMaToHop(maToHop.isEmpty() ? extractMa(maToHopRaw) : maToHop);
                nth.setTbKeys(tbKeys.isEmpty() ? maNganh + "_" + nth.getMaToHop() : tbKeys);
                nth.setDoLech(doLech);
                parseSubjects(maToHopRaw, nth);

                nganhToHopRepository.save(nth);
                existingKeys.add(nth.getTbKeys());
                them++;

                // Cập nhật tổ hợp gốc vào xt_nganh
                if ("Gốc".equalsIgnoreCase(goc)) {
                    nganhRepository.findByMaNganh(maNganh).ifPresent(n -> {
                        n.setToHopGoc(nth.getMaToHop());
                        nganhRepository.save(n);
                    });
                    capNhatGoc++;
                }
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return String.format("Thêm mới: %d tổ hợp. Cập nhật tổ hợp gốc: %d ngành. Bỏ qua: %d.",
                them, capNhatGoc, boQua);
    }

    // ──────────────────────────────────────────────
    // 4. Import Ds thi sinh.xlsx
    //    → xt_thisinhxettuyen25 + xt_diemthixettuyen
    // ──────────────────────────────────────────────
    @Transactional
    public String importThiSinh(MultipartFile file) {
        int themTs = 0, themDiem = 0, boQua = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            // Cache CCCD đã có để tránh N+1 check
            Set<String> existingCccd = new HashSet<>(
                    thiSinhRepository.findAll()
                            .stream().map(ThiSinh::getCccd).filter(Objects::nonNull).toList()
            );
            Set<String> existingDiem = new HashSet<>(
                    diemThiRepository.findAll()
                            .stream().map(DiemThiXetTuyen::getCccd).filter(Objects::nonNull).toList()
            );

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cccd = fmt.formatCellValue(row.getCell(1)).trim();
                if (cccd.isEmpty()) continue;

                // Thí sinh
                if (!existingCccd.contains(cccd)) {
                    String fullName = fmt.formatCellValue(row.getCell(2)).trim();
                    String ho = "", ten = fullName;
                    int idx = fullName.lastIndexOf(" ");
                    if (idx != -1) { ho = fullName.substring(0, idx); ten = fullName.substring(idx + 1); }

                    String ngaySinh = fmt.formatCellValue(row.getCell(3)).trim();
                    String password = buildPassword(ngaySinh);

                    ThiSinh ts = new ThiSinh();
                    ts.setCccd(cccd);
                    ts.setHo(ho);
                    ts.setTen(ten);
                    ts.setNgaySinh(ngaySinh);
                    ts.setGioiTinh(fmt.formatCellValue(row.getCell(4)).trim());
                    ts.setDoiTuong(DT_MAP.getOrDefault(fmt.formatCellValue(row.getCell(5)).trim(), null));
                    ts.setKhuVuc(KV_MAP.getOrDefault(fmt.formatCellValue(row.getCell(6)).trim(), null));
                    ts.setPassword(password);
                    ts.setNoiSinh(fmt.formatCellValue(row.getCell(35)).trim());

                    thiSinhRepository.save(ts);
                    existingCccd.add(cccd);
                    themTs++;
                } else {
                    boQua++;
                }

                // Điểm thi (luôn import nếu chưa có)
                if (!existingDiem.contains(cccd)) {
                    DiemThiXetTuyen d = new DiemThiXetTuyen();
                    d.setCccd(cccd);
                    d.setDiemToan(parseDoubleSafe(fmt.formatCellValue(row.getCell(7))));   // TO
                    d.setDiemVan(parseDoubleSafe(fmt.formatCellValue(row.getCell(8))));    // VA
                    d.setDiemLy(parseDoubleSafe(fmt.formatCellValue(row.getCell(9))));     // LI
                    d.setDiemHoa(parseDoubleSafe(fmt.formatCellValue(row.getCell(10))));   // HO
                    d.setDiemSinh(parseDoubleSafe(fmt.formatCellValue(row.getCell(11))));  // SI
                    d.setDiemSu(parseDoubleSafe(fmt.formatCellValue(row.getCell(12))));    // SU
                    d.setDiemDia(parseDoubleSafe(fmt.formatCellValue(row.getCell(13))));   // DI
                    d.setN1Thi(parseDoubleSafe(fmt.formatCellValue(row.getCell(15))));     // NN (N1_THI)
                    d.setDiemKtpl(parseDoubleSafe(fmt.formatCellValue(row.getCell(17)))); // KTPL
                    d.setDiemTiengAnh(parseDoubleSafe(fmt.formatCellValue(row.getCell(18)))); // TI
                    d.setCncn(parseDoubleSafe(fmt.formatCellValue(row.getCell(19))));      // CNCN
                    d.setCnnn(parseDoubleSafe(fmt.formatCellValue(row.getCell(20))));      // CNNN

                    diemThiRepository.save(d);
                    existingDiem.add(cccd);
                    themDiem++;
                }
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return String.format(
                "Thí sinh: thêm %d, bỏ qua %d (đã có). Điểm thi: thêm %d bản ghi.",
                themTs, boQua, themDiem);
    }

    // ──────────────────────────────────────────────
    // 5. Import Nguyenvong.xlsx → xt_nguyenvongxettuyen
    // ──────────────────────────────────────────────
    @Transactional
    public String importNguyenVong(MultipartFile file) {
        int them = 0, boQua = 0;
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            DataFormatter fmt = new DataFormatter();
            Set<String> existingKeys = new HashSet<>(
                    nguyenVongRepository.findAll()
                            .stream().map(NguyenVongXetTuyen::getNvKeys).filter(Objects::nonNull).toList()
            );

            // Sheet1 và Sheet2 đều có cùng format, header ở row 3 (index 3)
            for (int s = 0; s < Math.min(wb.getNumberOfSheets(), 3); s++) {
                Sheet sheet = wb.getSheetAt(s);
                if (sheet.getSheetName().equalsIgnoreCase("TKchung")) continue;

                for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String cccd     = fmt.formatCellValue(row.getCell(1)).trim();
                    String thuTuStr = fmt.formatCellValue(row.getCell(2)).trim();
                    String maNganh  = fmt.formatCellValue(row.getCell(5)).trim();

                    if (cccd.isEmpty() || maNganh.isEmpty()) continue;

                    int thuTu = parseIntSafe(thuTuStr);
                    String nvKeys = cccd + "_NV" + thuTu;

                    if (existingKeys.contains(nvKeys)) { boQua++; continue; }

                    NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
                    nv.setCccd(cccd);
                    nv.setMaNganh(maNganh);
                    nv.setThuTuNguyenVong(thuTu);
                    nv.setNvKeys(nvKeys);

                    nguyenVongRepository.save(nv);
                    existingKeys.add(nvKeys);
                    them++;
                }
            }
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
        return String.format("Thêm mới: %d nguyện vọng. Bỏ qua (đã có): %d.", them, boQua);
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────

    private String extractMa(String raw) {
        int idx = raw.indexOf("(");
        return idx != -1 ? raw.substring(0, idx).trim() : raw.trim();
    }

    private void parseSubjects(String raw, NganhToHop nth) {
        int open = raw.indexOf("("), close = raw.indexOf(")");
        if (open == -1 || close == -1) return;

        String[] parts = raw.substring(open + 1, close).split(",");
        for (int p = 0; p < parts.length && p < 3; p++) {
            String[] cv = parts[p].trim().split("-");
            String code = cv[0].trim().toUpperCase();
            int coeff = cv.length > 1 ? parseIntSafe(cv[1]) : 1;

            if (p == 0) { nth.setThMon1(code); nth.setHsMon1(coeff); }
            else if (p == 1) { nth.setThMon2(code); nth.setHsMon2(coeff); }
            else { nth.setThMon3(code); nth.setHsMon3(coeff); }

            switch (code) {
                case "TO"   -> nth.setTo(coeff);
                case "VA"   -> nth.setVa(coeff);
                case "LI"   -> nth.setLi(coeff);
                case "HO"   -> nth.setHo(coeff);
                case "SI"   -> nth.setSi(coeff);
                case "SU"   -> nth.setSu(coeff);
                case "DI"   -> nth.setDi(coeff);
                case "N1"   -> nth.setN1(coeff);
                case "TI"   -> nth.setTi(coeff);
                case "KTPL" -> nth.setKtpl(coeff);
                default     -> nth.setKhac(coeff);
            }
        }
    }

    private String buildPassword(String ngaySinh) {
        if (ngaySinh == null || ngaySinh.isBlank()) return "";
        if (ngaySinh.matches("\\d{2}/\\d{2}/\\d{4}")) return ngaySinh.replace("/", "");
        if (ngaySinh.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] p = ngaySinh.split("-");
            return p[2] + p[1] + p[0];
        }
        if (ngaySinh.matches("\\d{8}")) return ngaySinh;
        return "";
    }

    private int parseIntSafe(String s) {
        try { return (int) Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }
    }

    private Double parseDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return null; }
    }
}
