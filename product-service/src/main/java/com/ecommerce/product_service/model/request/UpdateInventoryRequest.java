package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInventoryRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0)
    private Integer quantity;

    @NotBlank(message = "Operation is required")
    private String operation;   // ADD | SUBTRACT | SET

    private String reason;
}