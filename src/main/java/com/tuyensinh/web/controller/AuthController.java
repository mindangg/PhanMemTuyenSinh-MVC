package com.tuyensinh.web.controller;

import com.tuyensinh.web.dto.LoginForm;
import com.tuyensinh.web.entity.ThiSinh;
import com.tuyensinh.web.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Vui lòng đăng nhập để tiếp tục.");
        }
        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute LoginForm form, HttpSession session, Model model) {
        ThiSinh ts = authService.login(form.getCccd(), form.getPassword());
        if (ts == null) {
            model.addAttribute("error", "Số CCCD hoặc mật khẩu không đúng.");
            model.addAttribute("loginForm", form);
            return "login";
        }
        session.setAttribute("thiSinh", ts);
        return "redirect:/tra-cuu-ket-qua";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
