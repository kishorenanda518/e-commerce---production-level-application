package com.ecommerce.order_service.kafka;

public class KafkaTopics {
    public static final String ORDER_PLACED    = "order.placed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_SHIPPED   = "order.shipped";
    public static final String ORDER_DELIVERED = "order.delivered";
    public static final String ORDER_CANCELLED = "order.cancelled";
}