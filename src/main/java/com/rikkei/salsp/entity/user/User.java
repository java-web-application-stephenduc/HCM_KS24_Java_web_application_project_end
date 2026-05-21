package com.rikkei.salsp.entity.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Thực thể User đại diện cho tài khoản người dùng trong hệ thống SALSP.
 *
 * MỤC ĐÍCH: Lưu trữ thông tin xác thực (email, mật khẩu băm) và vai trò phân quyền.
 *
 * QUAN HỆ DỮ LIỆU:
 * - @OneToOne mappedBy "user" -> UserProfile: Liên kết 1-1 với hồ sơ chi tiết
 *   CascadeType.ALL: Nếu xóa User, tự động xóa UserProfile liên kết (đảm bảo dữ liệu không đồng bộ)
 *
 * CHIẾN LƯỢC KHÓA (Pessimistic Locking): Sử dụng khi cần cập nhật password hoặc trạng thái active
 *   để tránh race condition nếu hai request cùng lúc thay đổi trạng thái.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Email - dùng làm tên đăng nhập (username) duy nhất trong hệ thống.
     * Được trim trước khi lưu để tránh leading/trailing spaces gây lỗi login.
     * UNIQUE constraint ở database level đảm bảo không trùng lặp.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Mật khẩu đã được mã hóa bằng BCrypt (không bao giờ lưu plaintext).
     * Độ mạnh BCrypt: 10 vòng lặp (PasswordEncoder trong SecurityConfig).
     * NEVER trả về DTO khi send response về client.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Vai trò phân quyền: STUDENT | LECTURER | ADMIN.
     * Dùng cho role-based access control (RBAC) ở SecurityConfig.
     * @Enumerated(EnumType.STRING) lưu tên enum thay vì index (tiện lợi cho maintenance).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * Trạng thái hoạt động của tài khoản.
     * active = false: Admin khóa tài khoản (user không thể login).
     * active = true: Tài khoản hoạt động bình thường.
     * Mặc định = true khi đăng ký mới.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Dấu thời gian tạo tài khoản.
     * @CreationTimestamp tự động set khi INSERT, không thể UPDATE.
     * Dùng cho audit trail (kiểm soát lịch sử).
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Hồ sơ chi tiết người dùng (họ tên, SĐT, avatar, chuyên môn).
     *
     * Mối quan hệ:
     * - mappedBy = "user": UserProfile giữ foreign key, User là phía sở hữu (owning side)
     * - CascadeType.ALL: Khi xóa User, CASCADE xóa UserProfile (để dữ liệu sạch)
     *
     * LƯU Ý: Fetch mặc định là LAZY (không load UserProfile khi query User).
     * Nếu cần join, dùng @EntityGraph ở Repository level để tối ưu query.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
}

