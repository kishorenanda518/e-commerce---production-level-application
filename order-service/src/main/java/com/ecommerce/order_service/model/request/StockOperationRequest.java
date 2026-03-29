package com.ecommerce.order_service.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockOperationRequest {
    private String productId;
    private Integer quantity;
    private String orderId;
    private String reason;
}