package com.ecommerce.order_service.model.response;

import com.ecommerce.order_service.enums.OrderStatus;
import com.ecommerce.order_service.enums.PaymentMethod;
import com.ecommerce.order_service.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String        id;
    private String        orderNumber;
    private String        userId;
    private OrderStatus   status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal    subtotal;
    private BigDecimal    discountAmount;
    private BigDecimal    shippingAmount;
    private BigDecimal    taxAmount;
    private BigDecimal    totalAmount;
    private String        couponCode;
    private List<OrderItemResponse> items;
    private ShippingAddressResponse shippingAddress;
    private String        trackingNumber;
    private Instant       estimatedDelivery;
    private Instant       deliveredAt;
    private String        notes;
    private Instant       createdAt;
    private Instant       updatedAt;
}