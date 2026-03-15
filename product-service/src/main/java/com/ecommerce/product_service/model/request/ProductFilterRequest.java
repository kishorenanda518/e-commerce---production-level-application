package com.ecommerce.product_service.model.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductFilterRequest {
    private String     q;
    private String     categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String     brand;
    private Boolean    inStock;
    private Double     minRating;
    private List<String> tags;
    private String     sort = "newest";
    private int        page = 0;
    private int        size = 20;
}