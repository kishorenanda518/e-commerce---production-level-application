package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class StockUpdatedEvent {
    private String  productId;
    private Integer oldQty;
    private Integer newQty;
    private String  operation;
    private String  orderId;
    private Instant timestamp;
}