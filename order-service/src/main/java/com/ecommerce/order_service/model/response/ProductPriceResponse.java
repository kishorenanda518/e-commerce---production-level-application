package com.ecommerce.order_service.model.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductPriceResponse {
    private String     productId;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
}