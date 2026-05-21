package com.rikkei.salsp.exception;

/**
 * Exception khi email đã tồn tại (đăng ký duplicate account).
 *
 * MỤC ĐÍCH: Specific exception cho lỗi email trùng lặp.
 *
 * KỊCH BẢN:
 * - AuthService.register(): findByEmail(dto.getEmail()).isPresent()
 * → throw new DuplicateEmailException("Email đã tồn tại")
 *
 * HANDLER:
 * - GlobalExceptionHandler.handleDuplicateEmail()
 * - Redirect /auth/register với error flash message
 * - Form vẫn giữ data, user sửa email lại
 *
 * EXTENDS: BusinessException (business rule, không technical error)
 */
public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}

