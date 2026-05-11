package com.tuyensinh.web.controller;

import com.tuyensinh.web.dto.DgnlForm;
import com.tuyensinh.web.dto.KetQuaTinhDiem;
import com.tuyensinh.web.entity.Nganh;
import com.tuyensinh.web.repository.NganhRepository;
import com.tuyensinh.web.service.QuyDoiService;
import com.tuyensinh.web.service.TinhDiemXetTuyenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tinh-diem")
public class TinhDiemDgnlController {

    @Autowired private TinhDiemXetTuyenService tinhDiemService;
    @Autowired private NganhRepository nganhRepository;

    @GetMapping("/dgnl")
    public String formDgnl(Model model) {
        List<Nganh> nganhList = nganhRepository.findAllByOrderByMaNganhAsc();
        model.addAttribute("nganhList", nganhList);
        model.addAttribute("form", new DgnlForm());
        model.addAttribute("mucCC", QuyDoiService.MucChungChi.values());
        return "tinh-diem-dgnl";
    }

    @PostMapping("/dgnl")
    public String tinhDgnl(@ModelAttribute DgnlForm form, Model model) {
        List<Nganh> nganhList = nganhRepository.findAllByOrderByMaNganhAsc();
        model.addAttribute("nganhList", nganhList);
        model.addAttribute("form", form);
        model.addAttribute("mucCC", QuyDoiService.MucChungChi.values());

        if (form.getDiemDgnl() < 0 || form.getDiemDgnl() > 1200) {
            model.addAttribute("loi", "Điểm ĐGNL phải trong khoảng 0 – 1200.");
            return "tinh-diem-dgnl";
        }
        KetQuaTinhDiem ketQua = tinhDiemService.tinhDiemDgnl(form);
        model.addAttribute("ketQua", ketQua);
        return "tinh-diem-dgnl";
    }
}
