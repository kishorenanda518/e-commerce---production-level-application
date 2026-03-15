package com.ecommerce.product_service.exception;
public class ImageLimitException extends RuntimeException {
    public ImageLimitException(String message) { super(message); }
}