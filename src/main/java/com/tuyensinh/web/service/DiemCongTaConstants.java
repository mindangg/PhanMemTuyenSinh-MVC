package com.tuyensinh.web.service;

import java.util.Map;

/**
 * Bảng quy đổi chứng chỉ tiếng Anh → điểm.
 * Nguồn: docs/bangQUyDoiTA_2025.docx
 */
public final class DiemCongTaConstants {

    private DiemCongTaConstants() {}

    // Điểm quy đổi môn Tiếng Anh khi tổ hợp xét tuyển CÓ môn TA
    public static final Map<QuyDoiService.MucChungChi, Double> DIEM_QD_MON = Map.of(
            QuyDoiService.MucChungChi.MUC1, 8.0,
            QuyDoiService.MucChungChi.MUC2, 9.0,
            QuyDoiService.MucChungChi.MUC3, 10.0
    );

    // Điểm cộng vào THXT (thang 30) khi tổ hợp xét tuyển KHÔNG có môn TA
    public static final Map<QuyDoiService.MucChungChi, Double> DIEM_CONG_THPT = Map.of(
            QuyDoiService.MucChungChi.MUC1, 1.0,
            QuyDoiService.MucChungChi.MUC2, 1.5,
            QuyDoiService.MucChungChi.MUC3, 2.0
    );

    // Điểm cộng vào điểm ĐGNL (thang 1200) khi tổ hợp KHÔNG có môn TA
    public static final Map<QuyDoiService.MucChungChi, Double> DIEM_CONG_DGNL = Map.of(
            QuyDoiService.MucChungChi.MUC1, 40.0,
            QuyDoiService.MucChungChi.MUC2, 60.0,
            QuyDoiService.MucChungChi.MUC3, 80.0
    );
}
