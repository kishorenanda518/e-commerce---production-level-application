package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private String               id;
    private String               name;
    private String               slug;
    private String               description;
    private String               parentId;
    private String               imageUrl;
    private Integer              displayOrder;
    private Boolean              isActive;
}