package com.ecommerce.order_service.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReserveStockRequest {
    private String productId;
    private Integer quantity;
    private String orderId;
}