package com.ecommerce.order_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class OrderStatusChangedEvent {
    private String  orderId;
    private String  orderNumber;
    private String  userId;
    private String  oldStatus;
    private String  newStatus;
    private String  reason;
    private Instant timestamp;
}