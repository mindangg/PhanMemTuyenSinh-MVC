package com.tuyensinh.web.controller;

import com.tuyensinh.web.dto.KetQuaTinhDiem;
import com.tuyensinh.web.dto.ThptVsatForm;
import com.tuyensinh.web.entity.Nganh;
import com.tuyensinh.web.entity.NganhToHop;
import com.tuyensinh.web.repository.NganhRepository;
import com.tuyensinh.web.repository.NganhToHopRepository;
import com.tuyensinh.web.service.QuyDoiService;
import com.tuyensinh.web.service.TinhDiemXetTuyenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tinh-diem")
public class TinhDiemThptController {

    private static final Logger log = LoggerFactory.getLogger(TinhDiemThptController.class);

    @Autowired private TinhDiemXetTuyenService tinhDiemService;
    @Autowired private NganhRepository nganhRepository;
    @Autowired private NganhToHopRepository nganhToHopRepository;

    private static final String[] DS_MON = {"TO", "LI", "HO", "SI", "SU", "DI", "VA", "TI", "KTPL"};
    private static final String[] TEN_MON = {"Toán", "Lý", "Hóa", "Sinh", "Sử", "Địa", "Văn", "Tiếng Anh", "KTPL"};

    @GetMapping("/thpt-vsat")
    public String formThpt(Model model) {
        populateCommon(model, null);
        model.addAttribute("form", new ThptVsatForm());
        return "tinh-diem-thpt-vsat";
    }

    @PostMapping("/thpt-vsat")
    public String tinhThpt(
            @RequestParam Map<String, String> allParams,
            @ModelAttribute ThptVsatForm form,
            Model model) {

        // Đọc điểm từng môn từ params (tên field: mon_TO, mon_LI, ...)
        Map<String, Double> diemMon = new HashMap<>();
        for (String maMon : DS_MON) {
            String val = allParams.get("mon_" + maMon);
            if (val != null && !val.isBlank()) {
                try { diemMon.put(maMon, Double.parseDouble(val)); } catch (NumberFormatException e) {
                    log.warn("Điểm môn {} không hợp lệ, bỏ qua giá trị: '{}'", maMon, val);
                }
            }
        }
        form.setDiemCacMon(diemMon);

        populateCommon(model, form.getMaNganh());
        model.addAttribute("form", form);
        model.addAttribute("mucCC", QuyDoiService.MucChungChi.values());

        List<KetQuaTinhDiem> ketQuaList = tinhDiemService.tinhDiemThptVsat(form);
        model.addAttribute("ketQuaList", ketQuaList);
        return "tinh-diem-thpt-vsat";
    }

    private void populateCommon(Model model, String selectedNganh) {
        List<Nganh> nganhList = nganhRepository.findAllByOrderByMaNganhAsc();
        model.addAttribute("nganhList", nganhList);
        model.addAttribute("dsMon", DS_MON);
        model.addAttribute("tenMon", TEN_MON);
        model.addAttribute("mucCC", QuyDoiService.MucChungChi.values());

        // Lấy danh sách tổ hợp của ngành (AJAX hoặc mặc định ngành đầu)
        List<NganhToHop> toHopList = null;
        if (selectedNganh != null && !selectedNganh.isBlank()) {
            toHopList = nganhToHopRepository.findByMaNganh(selectedNganh);
        }
        model.addAttribute("toHopList", toHopList);
    }
}
