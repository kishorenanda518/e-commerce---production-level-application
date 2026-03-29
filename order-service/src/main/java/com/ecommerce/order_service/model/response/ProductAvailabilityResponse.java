package com.ecommerce.order_service.model.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductAvailabilityResponse {
    private String     productId;
    private String     name;
    private String     sku;
    private boolean    available;
    private int        stock;
    private BigDecimal price;
    private String     primaryImageUrl;
}