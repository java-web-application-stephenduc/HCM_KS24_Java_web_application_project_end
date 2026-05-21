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
 * Controller xử lý các endpoint xác thực người dùng (login, register, logout).
 *
 * KIẾN TRÚC:
 * - GET /auth/login: Hiển thị form đăng nhập
 * - POST /auth/login: Spring Security tự xử lý (định tuyến ở SecurityConfig)
 * - GET /auth/register: Hiển thị form đăng ký
 * - POST /auth/register: Xử lý form đăng ký (gọi AuthService)
 * - GET /auth/logout: Spring Security tự xử lý
 *
 * SESSION MANAGEMENT:
 * - Tạo session trước khi render Thymeleaf để tránh IllegalStateException với CSRF token
 * - CSRF protection bật mặc định trên POST (cấu hình ở SecurityConfig)
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint GET /auth/login - Hiển thị trang đăng nhập.
     *
     * FLOW:
     * 1. Tạo session trước (request.getSession(true)) để Thymeleaf có thể resolve CSRF token
     * 2. Trả về template "auth/login"
     * 3. Template hiển thị form với 2 fields: email, password
     * 4. Client submit -> POST /auth/login (handled by Spring Security FormLoginConfigurer)
     * 5. Spring Security dùng CustomUserDetailsService + CustomAuthSuccessHandler/CustomAuthFailureHandler
     *
     * @param request HttpServletRequest - dùng để tạo session
     * @return String tên template "auth/login"
     */
    @GetMapping("/auth/login")
    public String login(jakarta.servlet.http.HttpServletRequest request) {
        // Tạo session trước khi render view (tránh lỗi CSRF token không được resolve)
        // Nếu session đã tồn tại, sẽ trả về session cũ
        request.getSession(true);
        return "auth/login";
    }

    /**
     * Endpoint GET /auth/register - Hiển thị form đăng ký tài khoản mới.
     *
     * FLOW:
     * 1. Tạo đối tượng RegisterDto trống
     * 2. Gửi DTO xuống view để Thymeleaf bind form fields
     * 3. Tạo session cho CSRF token
     * 4. Trả về template "auth/register"
     * 5. Client nhập thông tin và submit -> POST /auth/register
     *
     * @param model Model - container để truyền dữ liệu xuống view
     * @param request HttpServletRequest - tạo session
     * @return String tên template "auth/register"
     */
    @GetMapping("/auth/register")
    public String registerForm(Model model, jakarta.servlet.http.HttpServletRequest request) {
        // Tạo session để Thymeleaf xử lý CSRF token
        request.getSession(true);

        // Gửi RegisterDto trống xuống view để Thymeleaf bind form data
        // Template dùng th:object="${registerDto}" để 2-way binding
        model.addAttribute("registerDto", new RegisterDto());

        return "auth/register";
    }

    /**
     * Endpoint POST /auth/register - Xử lý form đăng ký tài khoản.
     *
     * LUỒNG XỬ LÝ:
     * 1. Client gửi form với email, password, confirmPassword, fullName, phone
     * 2. Spring Validation (@Valid) kiểm tra:
     *    - email: @Email + @NotBlank (format email + không trống)
     *    - password: @Size(min=8) + @NotBlank (ít nhất 8 ký tự)
     *    - confirmPassword: @NotBlank
     *    - fullName: @NotBlank
     * 3. Nếu validation fail (errors.hasErrors() = true):
     *    - Quay lại form với thông báo lỗi
     *    - Không gọi AuthService
     * 4. Nếu validation thành công:
     *    - Gọi authService.register(dto) để tạo User + UserProfile
     *    - Nếu lỗi BusinessException (duplicate email, password mismatch):
     *      * Catch exception và thêm error vào model
     *      * Quay lại form để user sửa
     *    - Nếu thành công:
     *      * Thêm flash message "Đăng ký thành công. Vui lòng đăng nhập."
     *      * Redirect /auth/login để user truy cập form đăng nhập
     *
     * VALIDATION: Cấp độ Server (Spring Validation)
     * - @Valid: Trigger validation trên RegisterDto
     * - BindingResult errors: Chứa kết quả validate (lỗi nếu có)
     *
     * EXCEPTION HANDLING:
     * - DuplicateEmailException: Email đã tồn tại
     * - BusinessException: Mật khẩu không khớp
     *
     * @param dto RegisterDto với dữ liệu từ form
     * @param errors BindingResult chứa lỗi validation (nếu có)
     * @param flash RedirectAttributes - container thông báo khi redirect
     * @return String tên template hoặc redirect URL
     */
    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                           BindingResult errors,
                           RedirectAttributes flash) {
        // KIỂM TRA 1: Validation fail?
        // BindingResult.hasErrors() trả về true nếu có lỗi validation
        if (errors.hasErrors()) {
            // Quay lại form với các lỗi được tự động render bởi template
            return "auth/register";
        }

        // KIỂM TRA 2: Xử lý đăng ký
        try {
            // Gọi service để tạo User + UserProfile trong một transaction
            authService.register(dto);
        } catch (BusinessException ex) {
            // Bắt lỗi nghiệp vụ (duplicate email, password mismatch)
            // Thêm error vào BindingResult để template render thông báo
            errors.reject("registerError", ex.getMessage());
            return "auth/register";
        }

        // KIỂM TRA 3: Đăng ký thành công
        // Thêm flash message (flash scope: chỉ tồn tại sau 1 redirect)
        // Template login sẽ kiểm tra existence ${success} và hiển thị thông báo
        flash.addFlashAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");

        // Redirect sang login page (client sẽ thấy flash message)
        return "redirect:/auth/login";
    }

    /**
     * Endpoint GET /auth/locked - Hiển thị trang thông báo tài khoản bị khóa.
     *
     * USE CASE:
     * - Admin khóa tài khoản (User.active = false)
     * - User cố gắng login lại
     * - CustomAuthFailureHandler detect và redirect tới endpoint này
     *
     * @return String tên template "auth/locked"
     */
    @GetMapping("/auth/locked")
    public String locked() {
        return "auth/locked";
    }
}

