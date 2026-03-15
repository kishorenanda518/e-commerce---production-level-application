package com.ecommerce.product_service.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateProductRequest {

    @NotEmpty(message = "Product list cannot be empty")
    @Size(max = 200, message = "Cannot create more than 200 products at once")
    @Valid
    private List<CreateProductRequest> products;
}