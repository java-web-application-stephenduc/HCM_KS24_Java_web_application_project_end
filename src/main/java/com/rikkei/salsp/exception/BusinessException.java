package com.rikkei.salsp.exception;

/**
 * Exception cha cho tất cả lỗi nghiệp vụ (business logic violations).
 *
 * MỤC ĐÍCH: Phân biệt với tưối ngoại lệ kỹ thuật (IO, SQL, v.v.).
 *
 * KỊCH BẢN SỬ DỤNG:
 * - Validation fail: "Mật khẩu không khớp"
 * - Business rule violate: "Không thể đặt lịch trong quá khứ"
 * - Domain invariant: "Email đã tồn tại"
 * - Insufficient resources: "Không đủ thiết bị mượn"
 *
 * CÁCH DÙNG:
 * throw new BusinessException("Lý do cụ thể");
 * → GlobalExceptionHandler.handleBusiness() → Redirect + flash message
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

