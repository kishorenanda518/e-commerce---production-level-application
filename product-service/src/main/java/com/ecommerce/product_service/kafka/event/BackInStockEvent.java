package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class BackInStockEvent {
    private String  productId;
    private String  name;
    private Integer newQty;
    private Instant timestamp;
}