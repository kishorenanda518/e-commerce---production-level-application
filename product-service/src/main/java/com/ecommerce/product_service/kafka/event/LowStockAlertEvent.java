package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class LowStockAlertEvent {
    private String  productId;
    private String  name;
    private Integer currentQty;
    private Integer threshold;
    private Instant timestamp;
}