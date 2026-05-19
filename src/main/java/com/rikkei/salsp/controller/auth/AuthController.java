package com.rikkei.salsp.controller.auth;

import com.rikkei.salsp.dto.auth.RegisterDto;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các nghiệp vụ xác thực người dùng bao gồm đăng nhập, đăng ký và đăng xuất.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Hiển thị trang đăng nhập.
     * 
     * @param request HTTP request dùng để tạo session trước khi render
     * @return Tên template đăng nhập
     */
    @GetMapping("/auth/login")
    public String login(jakarta.servlet.http.HttpServletRequest request) {
        request.getSession(true); // Tạo session trước để tránh IllegalStateException khi CSRF token được giải quyết sau khi response commit
        return "auth/login";
    }

    /**
     * Hiển thị form đăng ký tài khoản.
     * 
     * @param model Model để truyền dữ liệu xuống view
     * @param request HTTP request dùng để tạo session trước khi render
     * @return Tên template đăng ký
     */
    @GetMapping("/auth/register")
    public String registerForm(Model model, jakarta.servlet.http.HttpServletRequest request) {
        request.getSession(true); // Tạo session trước để tránh IllegalStateException khi CSRF token được giải quyết sau khi response commit
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

