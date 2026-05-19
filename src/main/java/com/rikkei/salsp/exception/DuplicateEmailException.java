package com.rikkei.salsp.exception;

/**
 * Lớp `DuplicateEmailException` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}

