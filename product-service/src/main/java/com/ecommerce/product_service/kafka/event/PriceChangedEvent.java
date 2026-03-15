package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data @Builder
public class PriceChangedEvent {
    private String     productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Instant    timestamp;
}