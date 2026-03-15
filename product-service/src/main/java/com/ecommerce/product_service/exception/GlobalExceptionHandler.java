package com.ecommerce.product_service.exception;


import com.ecommerce.product_service.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        log.warn("Product not found [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(
            CategoryNotFoundException ex, HttpServletRequest request) {
        log.warn("Category not found [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadyExists(
            ProductAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Product conflict [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotEmpty(
            CategoryNotEmptyException ex, HttpServletRequest request) {
        log.warn("Category not empty [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {
        log.warn("Insufficient stock [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPrice(
            InvalidPriceException ex, HttpServletRequest request) {
        log.warn("Invalid price [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ImageLimitException.class)
    public ResponseEntity<ErrorResponse> handleImageLimit(
            ImageLimitException ex, HttpServletRequest request) {
        log.warn("Image limit [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleReviewExists(
            ReviewAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Review exists [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> ErrorResponse.FieldError.builder()
                        .field(e.getField())
                        .rejectedValue(e.getRejectedValue())
                        .message(e.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.", request);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build());
    }
}
