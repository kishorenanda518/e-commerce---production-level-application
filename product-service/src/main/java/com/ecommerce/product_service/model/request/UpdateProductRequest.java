package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class UpdateProductRequest {

    @Size(min = 3, max = 200)
    private String name;

    @Size(min = 10, max = 5000)
    private String description;

    @Size(max = 500)
    private String shortDescription;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;
    private String     categoryId;

    @Size(max = 100)
    private String brandName;

    private List<String>        tags;
    private Double              weight;
    private Map<String, String> attributes;
}