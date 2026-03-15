package com.ecommerce.product_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data @Builder
public class ProductUpdatedEvent {
    private String      productId;
    private List<String> updatedFields;
    private Instant     timestamp;
}