package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000)
    private String description;

    @Size(max = 500)
    private String shortDescription;

    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]{5,30}$", message = "SKU must be uppercase alphanumeric (5-30 chars)")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @Size(max = 100)
    private String brandName;

    private List<String> tags;

    private Double weight;

    @Min(value = 0, message = "Initial stock cannot be negative")
    private int initialStock;

    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private int lowStockThreshold = 10;

    private List<String> imageUrls;

    private Map<String, String> attributes;
}