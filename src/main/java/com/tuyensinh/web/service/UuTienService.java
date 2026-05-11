package com.tuyensinh.web.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Điểm ưu tiên đối tượng + khu vực theo thang 30.
 * Nguồn: Điều 7 Quy chế tuyển sinh 2025.
 */
@Service
public class UuTienService {

    private static final double NGUONG_GIAM_UU_TIEN = 22.5;
    private static final double DIEM_TOI_DA = 30.0;
    private static final double BIEN_DO = 7.5;

    // Điểm ưu tiên đối tượng (thang 30)
    private static final Map<String, Double> UT_DOI_TUONG = Map.of(
            "UT1", 2.0, "UT2", 2.0, "UT3", 2.0, "UT4", 2.0,
            "UT5", 1.0, "UT6", 1.0, "UT7", 1.0
    );

    // Điểm ưu tiên khu vực (thang 30)
    private static final Map<String, Double> UT_KHU_VUC = Map.of(
            "KV1", 0.75, "KV2-NT", 0.5, "KV2", 0.25, "KV3", 0.0
    );

    /**
     * Tính MĐƯT = điểm ưu tiên đối tượng + khu vực (chưa scale theo công thức).
     */
    public double getMDUT(String doiTuong, String khuVuc) {
        double dtDiem = UT_DOI_TUONG.getOrDefault(doiTuong, 0.0);
        double kvDiem = UT_KHU_VUC.getOrDefault(khuVuc, 0.0);
        return dtDiem + kvDiem;
    }

    /**
     * Tính điểm ưu tiên thực tế (ĐƯT) theo công thức Quy chế:
     * - Nếu (ĐTHGXT + ĐC) < 22.5: ĐƯT = MĐƯT
     * - Nếu (ĐTHGXT + ĐC) >= 22.5: ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] * MĐƯT
     */
    public double tinhDUT(double dthgxt, double dc, String doiTuong, String khuVuc) {
        double mdut = getMDUT(doiTuong, khuVuc);
        if (mdut == 0) return 0;

        double tong = dthgxt + dc;
        if (tong < NGUONG_GIAM_UU_TIEN) {
            return mdut;
        } else {
            return Math.max(0, ((DIEM_TOI_DA - tong) / BIEN_DO) * mdut);
        }
    }
}
