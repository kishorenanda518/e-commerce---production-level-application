package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 120)
    private String slug;

    @Size(max = 500)
    private String description;

    private String parentId;

    @Size(max = 500)
    private String imageUrl;

    private Integer displayOrder = 0;
}