package com.ecommerce.product_service.model.request;

import com.ecommerce.product_service.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {

    @NotNull(message = "Status is required")
    private ProductStatus status;
}