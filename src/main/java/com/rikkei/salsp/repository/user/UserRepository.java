package com.rikkei.salsp.repository.user;

import com.rikkei.salsp.entity.user.User;
import com.rikkei.salsp.entity.user.UserRole;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý truy vấn dữ liệu User từ database.
 *
 * CHIẾN LƯỢC TỐI ƯU HIỆU NĂNG:
 * - Sử dụng @EntityGraph để xử lý lỗi N+1 Query khi load User + UserProfile
 * - Phân trang (Pageable) để xử lý danh sách người dùng lớn
 * - Custom queries để lọc theo role (STUDENT, LECTURER, ADMIN)
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Override findAll() với @EntityGraph để EAGER load UserProfile.
     *
     * VẤNĐỀ CẦN GIẢI QUYẾT:
     * - Mặc định: User có fetch=LAZY -> Query User + Query Profile (N+1 problem)
     * - Giải pháp: @EntityGraph("attributePaths = {"profile"}") -> LEFT OUTER JOIN
     *
     * TỐI ƯU: 1 query kết quả thay vì N+1 query.
     *
     * @param pageable Tham số phân trang (page, size, sort)
     * @return Page<User> danh sách người dùng kèm hồ sơ chi tiết
     */
    @Override
    @EntityGraph(attributePaths = {"profile"})
    Page<User> findAll(Pageable pageable);

    /**
     * Tìm kiếm người dùng bằng email (dùng cho login).
     *
     * USE CASE:
     * - Validate email tồn tại khi đăng ký mới (tránh duplicate)
     * - Load User khi đăng nhập (Spring Security dùng findByUsername)
     * - Kiểm tra email hợp lệ trước khi reset password
     *
     * @param email Email người dùng (case-sensitive so với database)
     * @return Optional<User> chứa User nếu tìm thấy, empty nếu không có
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm danh sách người dùng theo vai trò và phân trang.
     *
     * USE CASE:
     * - Admin xem danh sách Giảng viên để phê duyệt tài khoản
     * - Admin xem danh sách Sinh viên để quản lý
     * - Thống kê số lượng người dùng theo role
     *
     * HIỆU NĂNG: Spring Data JPA tự generate query với WHERE role = ?
     *
     * @param role Vai trò cần tìm (STUDENT, LECTURER, ADMIN)
     * @param pageable Phân trang (mặc định 20 record/page)
     * @return Page<User> danh sách người dùng theo role
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Đếm số lượng tài khoản đang hoạt động (active = true).
     *
     * USE CASE:
     * - Thống kê Dashboard loại ra tài khoản bị khóa
     * - Báo cáo hệ thống số lượng người dùng thực tế
     *
     * @return long số lượng tài khoản active
     */
    long countByActiveTrue();

    /**
     * Đếm số lượng người dùng theo vai trò.
     *
     * USE CASE:
     * - Thống kê: Bao nhiêu Sinh viên, bao nhiêu Giảng viên, bao nhiêu Admin
     * - Dashboard kinh kinh quản trị hiển thị tổng quan
     *
     * @param role Vai trò cần đếm
     * @return long số lượng người dùng có role đó
     */
    long countByRole(UserRole role);
}
