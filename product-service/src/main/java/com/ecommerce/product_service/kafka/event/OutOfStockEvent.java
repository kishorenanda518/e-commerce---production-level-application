package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class OutOfStockEvent {
    private String  productId;
    private String  name;
    private Instant timestamp;
}