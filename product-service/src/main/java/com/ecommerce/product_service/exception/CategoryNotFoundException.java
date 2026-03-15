package com.ecommerce.product_service.exception;
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) { super(message); }
}