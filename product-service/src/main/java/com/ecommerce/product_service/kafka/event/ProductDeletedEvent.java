package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class ProductDeletedEvent {
    private String  productId;
    private String  sku;
    private Instant timestamp;
}