package com.tuyensinh.web.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Bảng độ lệch điểm giữa các tổ hợp THPT.
 * Nguồn: docs/cac cong thuc tinh.docx – mục 2.1.
 *
 * Công thức: diem_quy_doi = diem_to_hop_thi - doLech[to_hop_goc][to_hop_thi]
 *
 * Ví dụ: A01 (điểm 20) → A00: quy đổi = 20 - (-0.69) = 20.69
 */
@Service
public class DoLechToHopService {

    // dolech[tohopGoc][tohopThi] = mức chênh lệch
    private static final Map<String, Map<String, Double>> TABLE = new HashMap<>();

    static {
        // Hàng 1 – gốc A00: cột A01, B00, C00, C01, D01, D07
        TABLE.put("A00", Map.of("A01", -0.69, "B00", -1.21, "C00", 2.32, "C01", 0.94, "D01", -0.68, "D07", -1.62));
        // Hàng 2 – gốc A01
        TABLE.put("A01", Map.of("A00", 0.69, "B00", -0.52, "C00", 3.01, "C01", 1.63, "D01", 0.01, "D07", -0.93));
        // Hàng 3 – gốc B00
        TABLE.put("B00", Map.of("A00", 1.21, "A01", 0.52, "C00", 3.53, "C01", 2.15, "D01", 0.53, "D07", -0.41));
        // Hàng 4 – gốc C00
        TABLE.put("C00", Map.of("A00", -2.32, "A01", -3.01, "B00", -3.53, "C01", -1.38, "D01", -3.00, "D07", -3.94));
        // Hàng 5 – gốc C01
        TABLE.put("C01", Map.of("A00", -0.94, "A01", -1.63, "B00", -2.15, "C00", 1.38, "D01", -1.62, "D07", -2.56));
        // Hàng 6 – gốc D01
        TABLE.put("D01", Map.of("A00", 0.68, "A01", -0.01, "B00", -0.53, "C00", 3.0, "C01", 1.62, "D07", -0.94));
    }

    /**
     * Quy đổi điểm tổ hợp xét tuyển về tổ hợp gốc của ngành.
     *
     * @param toHopGoc   mã tổ hợp gốc của ngành (VD: "A00")
     * @param toHopThi   mã tổ hợp thí sinh thi (VD: "A01")
     * @param diemToHop  điểm tổ hợp thí sinh thi
     * @return điểm đã quy đổi về tổ hợp gốc
     */
    public double quyDoiVeToHopGoc(String toHopGoc, String toHopThi, double diemToHop) {
        if (toHopGoc == null || toHopThi == null) return diemToHop;
        if (toHopGoc.equalsIgnoreCase(toHopThi)) return diemToHop;

        Map<String, Double> row = TABLE.get(toHopGoc.toUpperCase());
        if (row == null) return diemToHop; // tổ hợp gốc không trong bảng → độ lệch 0

        Double doLech = row.get(toHopThi.toUpperCase());
        if (doLech == null) return diemToHop; // tổ hợp thi không trong bảng → độ lệch 0

        return diemToHop - doLech;
    }
}
