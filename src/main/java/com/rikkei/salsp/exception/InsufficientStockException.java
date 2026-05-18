package com.rikkei.salsp.exception;

/**
 * Bug #24: kế thừa BusinessException thay vì RuntimeException.
 * Đảm bảo GlobalExceptionHandler bắt chính xác loại exception này,
 * và controller có thể catch qua BusinessException để xử lý inline.
 */
public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

