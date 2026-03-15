package com.ecommerce.product_service.model.response;

import com.ecommerce.product_service.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private String        id;
    private String        name;
    private String        shortDescription;
    private String        sku;
    private BigDecimal    price;
    private BigDecimal    compareAtPrice;
    private String        categoryId;
    private String        categoryName;
    private String        brandName;
    private ProductStatus status;
    private Double        averageRating;
    private Integer       reviewCount;
    private Long          soldCount;
    private List<String>  imageUrls;
    private Boolean       inStock;
    private Instant       createdAt;
}