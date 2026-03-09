package com.ecommerce.user_service.exception;

// ── Thrown when email or username already exists ─────────────────────
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}