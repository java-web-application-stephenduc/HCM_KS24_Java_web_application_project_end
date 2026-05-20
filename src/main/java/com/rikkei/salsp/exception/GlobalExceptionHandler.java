package com.rikkei.salsp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Lớp `GlobalExceptionHandler` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi không tìm thấy tài nguyên (404).
     * 
     * @param ex Lỗi văng ra
     * @param model Model để truyền message lỗi
     * @return Trang thông báo lỗi 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Xử lý các lỗi nghiệp vụ chung, trả về trang trước đó kèm thông báo.
     * 
     * @param ex Lỗi nghiệp vụ
     * @param flash Chứa thông báo lỗi
     * @param request Yêu cầu HTTP để lấy trang gốc
     * @return Chuyển hướng lại trang vừa truy cập
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes flash, HttpServletRequest request) {
        flash.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    /**
     * Xử lý trường hợp email đã được sử dụng khi đăng ký.
     * 
     * @param ex Lỗi trùng email
     * @param flash Chứa thông báo lỗi
     * @param request Yêu cầu HTTP
     * @return Chuyển hướng lại trang đăng ký
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmail(DuplicateEmailException ex, RedirectAttributes flash, HttpServletRequest request) {
        flash.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/register");
    }

    /**
     * Xử lý lỗi không có quyền truy cập (403 Forbidden).
     * 
     * @return Trang thông báo lỗi 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden() {
        return "error/403";
    }
}

