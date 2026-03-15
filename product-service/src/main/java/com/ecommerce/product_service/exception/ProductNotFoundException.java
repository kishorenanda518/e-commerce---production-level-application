package com.ecommerce.product_service.exception;
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) { super(message); }
}