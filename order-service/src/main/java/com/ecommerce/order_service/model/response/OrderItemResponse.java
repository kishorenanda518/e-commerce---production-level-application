package com.ecommerce.order_service.model.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private String     id;
    private String     productId;
    private String     productName;
    private String     productSku;
    private String     productImage;
    private Integer    quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}