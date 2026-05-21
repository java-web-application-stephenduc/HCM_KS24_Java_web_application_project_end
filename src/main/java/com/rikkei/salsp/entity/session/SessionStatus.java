package com.rikkei.salsp.entity.session;

/**
 * Enum định nghĩa vòng đời (state machine) của một buổi cố vấn:
 * <ul>
 *   <li>PENDING  — Sinh viên vừa đặt, chờ giảng viên xác nhận</li>
 *   <li>CONFIRMED — Giảng viên đã duyệt, buổi cố vấn chính thức</li>
 *   <li>COMPLETED — Buổi cố vấn đã diễn ra và kết thúc</li>
 *   <li>CANCELLED — Giảng viên hủy (có rejectionReason)</li>
 *   <li>REJECTED  — Giảng viên từ chối lúc đang PENDING</li>
 *   <li>CANCELED_BY_STUDENT — Sinh viên chủ động hủy</li>
 * </ul>
 */
public enum SessionStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    REJECTED,
    CANCELED_BY_STUDENT
}
