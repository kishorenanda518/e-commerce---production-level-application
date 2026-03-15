package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InventoryResponse {
    private String  id;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private String  warehouseLocation;
    private Instant lastUpdated;
}