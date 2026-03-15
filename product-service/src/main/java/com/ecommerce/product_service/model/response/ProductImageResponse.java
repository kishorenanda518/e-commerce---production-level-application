package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse {
    private String  id;
    private String  imageUrl;
    private String  altText;
    private Integer displayOrder;
    private Boolean isPrimary;
}