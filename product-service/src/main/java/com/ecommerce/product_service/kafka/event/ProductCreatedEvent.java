package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class ProductCreatedEvent {
    private String     productId;
    private String     name;
    private String     sku;
    private String     categoryId;
    private BigDecimal price;
    private Instant    timestamp;
}