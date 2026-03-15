package com.ecommerce.user_service.exception;

public class SamePasswordException extends RuntimeException {
    public SamePasswordException(String message) {
        super(message);
    }
}