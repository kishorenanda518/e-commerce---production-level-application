package com.ecommerce.product_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    // ── Constructor 1 — used everywhere: new ResourceNotFoundException("Category", "id", id)
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName    = fieldName;
        this.fieldValue   = fieldValue;
    }

    // ── Constructor 2 — simple message: new ResourceNotFoundException("Category not found")
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName    = null;
        this.fieldValue   = null;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName()    { return fieldName; }
    public Object getFieldValue()   { return fieldValue; }
}