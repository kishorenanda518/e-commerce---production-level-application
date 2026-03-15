package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 120)
    private String slug;

    @Size(max = 500)
    private String description;

    private String  parentId;

    @Size(max = 500)
    private String  imageUrl;

    private Integer displayOrder;
    private Boolean isActive;
}