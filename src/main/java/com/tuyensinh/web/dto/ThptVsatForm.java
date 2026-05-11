package com.tuyensinh.web.dto;

import com.tuyensinh.web.service.QuyDoiService;
import lombok.Data;

import java.util.Map;

@Data
public class ThptVsatForm {
    private String phuongThuc;         // "THPT" hoặc "VSAT"
    private String maNganh;
    private String doiTuong;           // UT1..UT7 hoặc rỗng
    private String khuVuc;             // KV1, KV2-NT, KV2, KV3
    private QuyDoiService.MucChungChi mucChungChiTA; // null nếu không có
    private Double diemCongBoSung;

    // Điểm từng môn: key = mã môn (TO, LI, HO, SI, SU, DI, VA, TI, KTPL)
    private Map<String, Double> diemCacMon;
}
