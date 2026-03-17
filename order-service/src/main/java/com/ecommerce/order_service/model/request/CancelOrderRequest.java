package com.ecommerce.order_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}