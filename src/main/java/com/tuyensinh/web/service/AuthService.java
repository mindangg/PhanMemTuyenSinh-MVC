package com.tuyensinh.web.service;

import com.tuyensinh.web.entity.ThiSinh;
import com.tuyensinh.web.repository.ThiSinhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    /**
     * Kiểm tra login: username = cccd, password = ngày sinh định dạng ddMMyyyy.
     * Ví dụ: sinh 15/03/2003 → password "15032003".
     * @return ThiSinh nếu đăng nhập đúng, null nếu sai.
     */
    @Transactional(readOnly = true)
    public ThiSinh login(String cccd, String password) {
        if (cccd == null || cccd.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        ThiSinh ts = thiSinhRepository.findByCccd(cccd.trim()).orElse(null);
        if (ts == null) return null;

        String expectedPassword = buildPasswordFromNgaySinh(ts.getNgaySinh());
        if (expectedPassword == null) return null;

        return expectedPassword.equals(password.trim()) ? ts : null;
    }

    /**
     * Chuyển ngaySinh (nhiều định dạng có thể) sang ddMMyyyy.
     * Thử các định dạng: dd/MM/yyyy, yyyy-MM-dd, ddMMyyyy.
     */
    private String buildPasswordFromNgaySinh(String ngaySinh) {
        if (ngaySinh == null || ngaySinh.isBlank()) return null;
        ngaySinh = ngaySinh.trim();

        // định dạng dd/MM/yyyy
        if (ngaySinh.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return ngaySinh.replace("/", "");
        }
        // định dạng yyyy-MM-dd
        if (ngaySinh.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = ngaySinh.split("-");
            return parts[2] + parts[1] + parts[0];
        }
        // định dạng ddMMyyyy (8 chữ số)
        if (ngaySinh.matches("\\d{8}")) {
            return ngaySinh;
        }
        return null;
    }
}
