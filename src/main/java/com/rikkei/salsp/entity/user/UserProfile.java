package com.rikkei.salsp.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thực thể UserProfile lưu trữ thông tin hồ sơ chi tiết của người dùng.
 *
 * MỤC ĐÍCH: Tách thông tin xác thực (User table) khỏi thông tin cá nhân (UserProfile table).
 *
 * THIẾT KẾ:
 * - 1-1 relationship với User
 * - Mỗi User bắt buộc có đúng 1 UserProfile (unique = true)
 * - Cascade = ALL ở User side: Xóa User → xóa Profile tự động
 *
 * LAZY FETCH:
 * - Mặc định không load UserProfile khi query User
 * - Khi cần load, dùng @EntityGraph hoặc JOIN FETCH ở Repository
 * - Tiết kiệm query khi chỉ cần email/role, không cần fullName
 *
 * USAGE:
 * - Hiển thị tên người dùng ở dashboard: user.getProfile().getFullName()
 * - Cập nhật avatar/SĐT sau khi đăng ký
 * - Dùng cho profile page (/profile)
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Liên kết 1-1 với tài khoản User (LAZY fetch).
     *
     * CONSTRAINTS:
     * - nullable = false: Mỗi Profile phải gắn với User (không orphan)
     * - unique = true: Mỗi User chỉ có 1 Profile (1-1, không 1-N)
     * - @JoinColumn: Foreign key tên "user_id" ở bảng user_profiles
     *
     * FETCH STRATEGY:
     * - LAZY: Chỉ load User khi gọi getUser()
     * - Repository dùng @EntityGraph(attributePaths = {"user"}) để eager load nếu cần
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Họ tên đầy đủ của người dùng.
     *
     * CONSTRAINTS:
     * - nullable = false: Bắt buộc khi đăng ký
     * - length = 150: Tên max 150 ký tự (hợp lý cho tên người Việt)
     *
     * PURPOSE:
     * - Hiển thị trên UI: "Xin chào, [fullName]!"
     * - Dùng cho report (tên giảng viên, sinh viên)
     * - Dùng cho email notification
     *
     * INITIALIZATION:
     * - Được set khi đăng ký (AuthService.register())
     * - Có thể update sau (/profile endpoint)
     */
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    /**
     * Số điện thoại liên hệ (tùy chọn).
     *
     * CONSTRAINTS:
     * - length = 20: Đủ cho số ĐT quốc tế (ví dụ: +84-0123-456-7890)
     * - Nullable (có thể không nhập lúc đăng ký)
     *
     * PURPOSE:
     * - Dùng để liên hệ khẩn cấp (nếu cần)
     * - Có thể NULL khi update profile (không bắt buộc)
     */
    @Column(length = 20)
    private String phone;

    /**
     * URL avatar người dùng (ảnh đại diện).
     *
     * FORMAT:
     * - Lưu URL path: "/uploads/avatars/user_5.jpg"
     * - Hoặc URL tuyệt đối: "https://example.com/avatars/user_5.jpg"
     * - length = 500: Đủ cho URL dài
     *
     * NULLABLE:
     * - Người dùng có thể không có avatar (sẽ dùng default avatar)
     *
     * FEATURE (Future):
     * - Cho phép upload avatar qua profile form
     * - Resize & optimize ảnh với ImageMagick
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
}

