package com.rikkei.salsp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) chứa dữ liệu từ form đăng ký tài khoản.
 *
 * MỤC ĐÍCH: Nhận dữ liệu từ client (HTML form), validate, sau đó chuyển đổi thành User + UserProfile entity.
 *
 * VALIDATION:
 * - Sử dụng Jakarta Validation Constraints (@NotBlank, @Email, @Size)
 * - Spring Controller tự động trigger validation khi @Valid được sử dụng
 * - Lỗi validation tự động bind vào BindingResult (không cần code thủ công)
 *
 * FLOW:
 * 1. Client submit form HTML -> Spring nhận dữ liệu
 * 2. Spring auto-bind dữ liệu vào RegisterDto properties
 * 3. Spring trigger validation (@Valid) -> BindingResult chứa lỗi nếu có
 * 4. Controller xử lý: nếu errors.hasErrors() -> quay lại form
 * 5. Nếu không lỗi -> pass DTO xuống AuthService.register()
 */
@Getter
@Setter
@NoArgsConstructor
public class RegisterDto {

    /**
     * Email người dùng - dùng làm tên đăng nhập.
     *
     * CONSTRAINTS:
     * - @Email: Validate format email (phải chứa @, domain, v.v.)
     * - @NotBlank: Không được trống, NULL cũng không được
     *
     * BUSINESS LOGIC:
     * - Server sẽ kiểm tra thêm: email không được trùng (qua UserRepository.findByEmail)
     * - Email được trim() trước khi lưu database
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Mật khẩu người dùng.
     *
     * CONSTRAINTS:
     * - @Size(min = 8): Ít nhất 8 ký tự (bảo mật cơ bản)
     * - @NotBlank: Không được trống
     *
     * BUSINESS LOGIC:
     * - Mặc định FormData HTML không encode -> gửi plaintext (đó là lý do cần HTTPS)
     * - Server sẽ mã hóa bằng BCrypt trước khi lưu database
     * - Mật khẩu NEVER được lưu plaintext
     * - Server sẽ so sánh với confirmPassword để đảm bảo user nhập đúng
     */
    @Size(min = 8)
    @NotBlank
    private String password;

    /**
     * Mật khẩu nhập lại - dùng để xác nhận người dùng nhập đúng password.
     *
     * CONSTRAINTS:
     * - @NotBlank: Không được trống
     *
     * BUSINESS LOGIC:
     * - Validation client-side: JavaScript kiểm tra password === confirmPassword
     * - Validation server-side: Trong AuthService.register(), kiểm tra:
     *   if (!dto.getPassword().equals(dto.getConfirmPassword())) { throw Exception; }
     * - Cần server-side check vì client-side có thể bị bypass
     */
    @NotBlank
    private String confirmPassword;

    /**
     * Tên đầy đủ của người dùng.
     *
     * CONSTRAINTS:
     * - @NotBlank: Không được trống
     *
     * BUSINESS LOGIC:
     * - Sẽ được lưu vào UserProfile.fullName
     * - Dùng để hiển thị trên trang profile, dashboard
     * - Được trim() trước khi lưu database
     */
    @NotBlank
    private String fullName;

    /**
     * Số điện thoại người dùng (tùy chọn).
     *
     * CONSTRAINTS: Không có (có thể NULL nếu client không nhập)
     *
     * BUSINESS LOGIC:
     * - Sẽ được lưu vào UserProfile.phone
     * - Dùng để liên hệ khi cần
     * - Có thể NULL hoặc rỗng
     */
    private String phone;
}

