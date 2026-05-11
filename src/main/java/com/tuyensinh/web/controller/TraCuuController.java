package com.tuyensinh.web.controller;

import com.tuyensinh.web.dto.KetQuaTraCuuVO;
import com.tuyensinh.web.entity.ThiSinh;
import com.tuyensinh.web.service.TraCuuKetQuaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TraCuuController {

    @Autowired
    private TraCuuKetQuaService traCuuKetQuaService;

    @GetMapping("/tra-cuu-ket-qua")
    public String traKetQua(HttpSession session, Model model) {
        ThiSinh thiSinh = (ThiSinh) session.getAttribute("thiSinh");
        // AuthFilter đã chặn nếu chưa login, nhưng double-check
        if (thiSinh == null) {
            return "redirect:/login";
        }

        List<KetQuaTraCuuVO> ketQuaList = traCuuKetQuaService.traKetQua(thiSinh.getCccd());
        model.addAttribute("thiSinh", thiSinh);
        model.addAttribute("ketQuaList", ketQuaList);
        return "tra-cuu-ket-qua";
    }
}
