package com.tuyensinh.web.dto;

import com.tuyensinh.web.service.QuyDoiService;
import lombok.Data;

@Data
public class DgnlForm {
    private double diemDgnl;          // 0 - 1200
    private String maNganh;
    private String doiTuong;           // UT1..UT7, rỗng nếu không có
    private String khuVuc;             // KV1, KV2-NT, KV2, KV3
    private QuyDoiService.MucChungChi mucChungChiTA; // null nếu không có CC TA
    private Double diemCongBoSung;     // điểm cộng tự bổ sung (nếu có)
}
