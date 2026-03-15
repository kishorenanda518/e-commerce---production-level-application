package com.ecommerce.product_service.exception;
public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException(String message) { super(message); }
}