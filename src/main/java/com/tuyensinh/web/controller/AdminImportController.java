package com.tuyensinh.web.controller;

import com.tuyensinh.web.service.ImportExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/import")
public class AdminImportController {

    @Autowired private ImportExcelService importExcelService;

    @GetMapping
    public String showPage() {
        return "admin-import";
    }

    @PostMapping("/chi-tieu")
    public String importChiTieu(@RequestParam MultipartFile file, RedirectAttributes ra) {
        ra.addFlashAttribute("ketQuaChiTieu", importExcelService.importChiTieu(file));
        return "redirect:/admin/import";
    }

    @PostMapping("/nguong")
    public String importNguong(@RequestParam MultipartFile file, RedirectAttributes ra) {
        ra.addFlashAttribute("ketQuaNguong", importExcelService.importNguong(file));
        return "redirect:/admin/import";
    }

    @PostMapping("/to-hop")
    public String importToHop(@RequestParam MultipartFile file, RedirectAttributes ra) {
        ra.addFlashAttribute("ketQuaToHop", importExcelService.importToHop(file));
        return "redirect:/admin/import";
    }

    @PostMapping("/thi-sinh")
    public String importThiSinh(@RequestParam MultipartFile file, RedirectAttributes ra) {
        ra.addFlashAttribute("ketQuaThiSinh", importExcelService.importThiSinh(file));
        return "redirect:/admin/import";
    }

    @PostMapping("/nguyen-vong")
    public String importNguyenVong(@RequestParam MultipartFile file, RedirectAttributes ra) {
        ra.addFlashAttribute("ketQuaNguyenVong", importExcelService.importNguyenVong(file));
        return "redirect:/admin/import";
    }
}
