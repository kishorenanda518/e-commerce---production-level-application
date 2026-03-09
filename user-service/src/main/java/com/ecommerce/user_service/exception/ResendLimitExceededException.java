package com.ecommerce.user_service.exception;

public class ResendLimitExceededException extends RuntimeException {

    public ResendLimitExceededException(String message) {
        super(message);
    }
}