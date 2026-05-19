package com.rikkei.salsp.exception;

/**
 * Lớp `BusinessException` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

