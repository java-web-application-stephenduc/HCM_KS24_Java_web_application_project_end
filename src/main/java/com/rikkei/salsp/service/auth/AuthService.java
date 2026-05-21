package com.rikkei.salsp.service.auth;

import com.rikkei.salsp.dto.auth.RegisterDto;
import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserProfile;
import com.rikkei.salsp.entity.user.UserRole;
import com.rikkei.salsp.exception.BusinessException;
import com.rikkei.salsp.exception.DuplicateEmailException;
import com.rikkei.salsp.repository.user.UserProfileRepository;
import com.rikkei.salsp.repository.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý quy trình xác thực và đăng ký tài khoản mới.
 *
 * TRÁCH NHIỆM:
 * - Xác thực email không trùng lặp (Business Validation)
 * - Mã hóa mật khẩu trước khi lưu database
 * - Tạo User + UserProfile trong một transaction (đảm bảo tính nhất quán)
 *
 * GIAO DỊCH: @Transactional đảm bảo nếu có lỗi ở bước sau, các bước trước sẽ rollback.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tìm kiếm người dùng bằng email.
     *
     * USE CASE:
     * - Validate email tồn tại khi check trước đăng ký
     * - Sử dụng cho CustomUserDetailsService để load User khi login
     *
     * @param email Email cần tìm kiếm
     * @return Optional<User> nếu tìm thấy, empty nếu không có
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Xử lý quy trình đăng ký tài khoản mới cho Sinh viên.
     *
     * LUỒNG NGHIỆP VỤ:
     * 1. Validate email không trùng lặp (tránh duplicate account)
     * 2. Validate 2 mật khẩu nhập vào phải giống nhau (client-side + server-side)
     * 3. Tạo User entity với:
     *    - Email được trim() để loại bỏ space (tránh lỗi login đặc biệt)
     *    - Mật khẩu được encode BCrypt (NEVER lưu plaintext)
     *    - Role = STUDENT (vai trò mặc định cho tất cả người đăng ký)
     *    - active = true (tài khoản hoạt động ngay lập tức)
     * 4. Tạo UserProfile liên kết với User
     * 5. Commit transaction (nếu lỗi, rollback cả User + Profile)
     *
     * EXCEPTION HANDLING:
     * - @throws DuplicateEmailException: Email đã tồn tại trong hệ thống
     * - @throws BusinessException: Mật khẩu không khớp hoặc validation fail
     *
     * @param dto Data Transfer Object chứa: email, password, confirmPassword, fullName, phone
     * @throws DuplicateEmailException nếu email đã tồn tại
     * @throws BusinessException nếu password không khớp
     */
    @Transactional
    public void register(RegisterDto dto) {
        // KIỂM TRA 1: Email đã tồn tại?
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email đã tồn tại");
        }

        // KIỂM TRA 2: Mật khẩu nhập lại có khớp không?
        // Lệnh này cần thực hiện server-side vì client có thể bị bypass
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu không khớp");
        }

        // BƯỚC 3: Tạo User entity
        // Trim email để loại bỏ leading/trailing spaces (tránh lỗi login với "email " vs "email")
        User user = new User();
        user.setEmail(dto.getEmail().trim());

        // Mã hóa mật khẩu: BCrypt với 10 vòng lặp (cấu hình ở SecurityConfig.passwordEncoder())
        // QUAN TRỌNG: Mật khẩu NEVER được lưu dạng plaintext
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // Tất cả người đăng ký qua form này được gán role STUDENT
        user.setRole(UserRole.STUDENT);

        // Tài khoản hoạt động mặc định (active = true)
        user.setActive(true);

        userRepository.save(user);

        // BƯỚC 4: Tạo UserProfile chi tiết
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        userProfileRepository.save(profile);

        // BƯỚC 5: Transaction commit tự động (nếu có exception, rollback cả User + Profile)
    }
}

