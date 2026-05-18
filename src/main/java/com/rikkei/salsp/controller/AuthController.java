package com.rikkei.salsp.controller;

import com.rikkei.salsp.dto.RegisterDto;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Hiển thị trang đăng nhập.
     * 
     * @return Tên template đăng nhập
     */
    @GetMapping("/auth/login")
    public String login(HttpServletRequest request) {
        request.getSession(true);
        return "auth/login";
    }

    /**
     * Hiển thị form đăng ký tài khoản.
     * 
     * @param model Model để truyền dữ liệu xuống view
     * @return Tên template đăng ký
     */
    @GetMapping("/auth/register")
    public String registerForm(HttpServletRequest request, Model model) {
        request.getSession(true);
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    /**
     * Xử lý yêu cầu đăng ký tài khoản mới.
     * 
     * @param dto Dữ liệu đăng ký từ form
     * @param errors Kết quả validate dữ liệu
     * @param flash Chứa thông báo flash
     * @return Chuyển hướng về trang đăng nhập nếu thành công
     */
    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                           BindingResult errors,
                           RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(dto);
        } catch (BusinessException ex) {
            errors.reject("registerError", ex.getMessage());
            return "auth/register";
        }
        flash.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
        return "redirect:/auth/login";
    }
}

