package com.rikkei.salsp.entity.user;

/**
 * Enum định nghĩa 3 vai trò người dùng trong hệ thống SALSP:
 * <ul>
 *   <li>STUDENT — Sinh viên: đặt lịch cố vấn, theo dõi lịch sử</li>
 *   <li>LECTURER — Giảng viên: duyệt lịch, đánh giá, cấp phát thiết bị</li>
 *   <li>ADMIN — Quản trị: quản lý người dùng, thiết bị, phiếu mượn</li>
 * </ul>
 */
public enum UserRole {
    STUDENT,
    LECTURER,
    ADMIN
}

