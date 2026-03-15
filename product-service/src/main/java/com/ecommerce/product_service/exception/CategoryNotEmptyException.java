package com.ecommerce.product_service.exception;
public class CategoryNotEmptyException extends RuntimeException {
    public CategoryNotEmptyException(String message) { super(message); }
}