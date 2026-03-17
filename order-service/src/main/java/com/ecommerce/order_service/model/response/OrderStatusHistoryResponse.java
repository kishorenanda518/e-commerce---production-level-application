package com.ecommerce.order_service.model.response;

import com.ecommerce.order_service.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class OrderStatusHistoryResponse {
    private String      id;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String      changedBy;
    private String      reason;
    private Instant     createdAt;
}