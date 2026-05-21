package com.rikkei.salsp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bộ xử lý ngoại lệ toàn cục (Global Exception Handler).
 *
 * MỤC ĐÍCH:
 * - Centralize exception handling cho toàn bộ ứng dụng
 * - Tránh duplicate try-catch ở mỗi controller
 * - Render error page hoặc error message nhất quán
 *
 * PATTERN: @ControllerAdvice + @ExceptionHandler
 * - @ControllerAdvice: Bean sẽ intercept exceptions từ tất cả controllers
 * - @ExceptionHandler: Method sẽ handle specific exception type
 *
 * FLOW:
 * 1. Controller throw Exception
 * 2. Spring không catch → tìm @ExceptionHandler match type
 * 3. Gọi handler method tương ứng
 * 4. Trả về view hoặc redirect
 *
 * EXCEPTION HIERARCHY:
 * ├── ResourceNotFoundException: Người dùng / session / record không tìm thấy
 * ├── BusinessException: Validation fail, logic error
 * ├── DuplicateEmailException: Email đã tồn tại
 * ├── SlotConflictException: Lịch trùng
 * ├── InsufficientStockException: Không đủ thiết bị mượn
 * └── Spring Built-in: AccessDeniedException (403), etc.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý ResourceNotFoundException (404 - Not Found).
     *
     * USE CASE:
     * - getById(id) không tìm thấy record
     * - User cố truy cập session / equipment không tồn tại
     * - CRUD endpoint không tìm thấy resource
     *
     * HANDLER LOGIC:
     * 1. Lấy message lỗi từ exception (ví dụ: "Không tìm thấy sinh viên")
     * 2. Gửi message vào Model
     * 3. Render template error/404.html
     * 4. Template hiển thị user-friendly error message
     *
     * @param ex ResourceNotFoundException (automatic inject by Spring)
     * @param model Model để truyền dữ liệu tới view
     * @return String tên template "error/404"
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Xử lý BusinessException (Generic business rule violation).
     *
     * USE CASE:
     * - Validate fail: "Giờ kết thúc phải sau giờ bắt đầu"
     * - Business rule: "Không thể đặt lịch trong quá khứ"
     * - Domain error: "Mật khẩu không khớp"
     *
     * HANDLER LOGIC:
     * 1. Lấy message lỗi
     * 2. Thêm vào RedirectAttributes (flash scope)
     * 3. Lấy referer header (trang trước đó)
     * 4. Redirect lại trang đó với erro message
     * 5. User nhìn thấy alert error + stay ở form (có thể sửa)
     *
     * REDIRECT FALLBACK:
     * - Nếu referer null (truy cập trực tiếp URL) → redirect /dashboard
     *
     * @param ex BusinessException
     * @param flash RedirectAttributes chứa flash message
     * @param request HttpServletRequest để lấy referer
     * @return "redirect:..." chuyển hướng + flash message
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes flash, HttpServletRequest request) {
        // Thêm lỗi vào flash scope (tồn tại sau redirect)
        flash.addFlashAttribute("error", ex.getMessage());

        // Lấy trang trước đó từ HTTP referer header
        String referer = request.getHeader("Referer");

        // Redirect lại trang cũ (+  flash message), hay /dashboard nếu không có referer
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    /**
     * Xử lý DuplicateEmailException (Email đã tồn tại).
     *
     * USE CASE:
     * - Sinh viên đăng ký với email đã được sử dụng
     * - Admin tạo user mới với email duplicate
     *
     * HANDLER LOGIC:
     * 1. Similar với BusinessException
     * 2. Thêm error message vào flash
     * 3. Redirect lại referer (trang register)
     * 4. User nhìn thấy error + form vẫn có data (có thể sửa email)
     *
     * @param ex DuplicateEmailException
     * @param flash RedirectAttributes
     * @param request HttpServletRequest
     * @return "redirect:..." quay lại form register
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmail(DuplicateEmailException ex, RedirectAttributes flash, HttpServletRequest request) {
        flash.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/auth/register");
    }

    /**
     * Xử lý AccessDeniedException (403 Forbidden).
     *
     * USE CASE:
     * - User login nhưng không đủ role (ví dụ: Student truy cập /admin/**)
     * - Spring Security block request
     *
     * HANDLER LOGIC:
     * 1. User đã authenticated (có session, token valid)
     * 2. Nhưng không có permission cho endpoint này
     * 3. Render error/403.html (forbidden page)
     * 4. User thấy "Bạn không có quyền truy cập trang này"
     *
     * @return String tên template "error/403"
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden() {
        return "error/403";
    }
}

