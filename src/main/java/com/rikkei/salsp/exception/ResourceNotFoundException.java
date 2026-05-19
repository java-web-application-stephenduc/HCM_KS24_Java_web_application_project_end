package com.rikkei.salsp.exception;

/**
 * Lớp `ResourceNotFoundException` thuộc hệ thống Smart Academic Lab Support Platform (SALSP).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

