package com.tuyensinh.web.dto;

import lombok.Data;

@Data
public class KetQuaTinhDiem {
    private String phuongThuc;
    private String maNganh;
    private String tenNganh;
    private String maToHop;

    private double diemToHop;       // điểm thô tổ hợp (trước khi nhân x3)
    private double diemQuyDoi30;    // ĐTHXT thang 30
    private double diemToHopGoc;    // ĐTHGXT sau quy đổi về tổ hợp gốc

    private double diemCong;        // ĐC (TA + tự bổ sung)
    private double diemUuTien;      // ĐƯT
    private double diemXetTuyen;    // ĐXT = ĐTHGXT + ĐC + ĐƯT

    private Double diemSan;         // ngưỡng đầu vào
    private Double diemTrungTuyen;  // điểm trúng tuyển (null nếu chưa công bố)

    private boolean datNguong;
    private boolean datTrungTuyen;
}
