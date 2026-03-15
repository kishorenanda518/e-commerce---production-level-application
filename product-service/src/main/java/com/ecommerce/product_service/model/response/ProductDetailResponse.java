package com.ecommerce.product_service.model.response;

import com.ecommerce.product_service.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductDetailResponse {
    private String               id;
    private String               name;
    private String               description;
    private String               shortDescription;
    private String               sku;
    private BigDecimal           price;
    private BigDecimal           compareAtPrice;
    private BigDecimal           costPrice;
    private String               categoryId;
    private String               categoryName;
    private String               brandName;
    private ProductStatus        status;
    private Double               averageRating;
    private Integer              reviewCount;
    private Long                 viewCount;
    private Long                 soldCount;
    private Double               weight;
    private List<String>         tags;
    private Map<String, String>  attributes;
    private List<ProductImageResponse> images;
    private InventoryResponse    inventory;
    private Instant              createdAt;
    private Instant              updatedAt;
}