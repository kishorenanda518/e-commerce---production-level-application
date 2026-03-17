package com.ecommerce.order_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class OrderPlacedEvent {
    private String     orderId;
    private String     orderNumber;
    private String     userId;
    private BigDecimal totalAmount;
    private Integer    itemCount;
    private Instant    timestamp;
}