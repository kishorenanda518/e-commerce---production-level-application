package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStatsResponse {
    private long total;
    private long active;
    private long draft;
    private long inactive;
    private long outOfStock;
    private long lowStock;
}