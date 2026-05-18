package com.rikkei.salsp.exception;

/**
 * Bug #24: kế thừa BusinessException thay vì RuntimeException.
 * Đảm bảo StudentBookingController có thể bắt cả SlotConflictException
 * và BusinessException bằng multi-catch mà không cần lặp code.
 */
public class SlotConflictException extends BusinessException {
    public SlotConflictException(String message) {
        super(message);
    }
}

