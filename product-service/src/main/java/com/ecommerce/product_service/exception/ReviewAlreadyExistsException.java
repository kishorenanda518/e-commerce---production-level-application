package com.ecommerce.product_service.exception;
public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String message) { super(message); }
}