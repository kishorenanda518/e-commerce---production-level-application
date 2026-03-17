package com.ecommerce.order_service.model.request;

import com.ecommerce.order_service.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String reason;
    private String trackingNumber;
}