package com.ecommerce.product_service.kafka;

public class KafkaTopics {
    public static final String PRODUCT_CREATED     = "product.created";
    public static final String PRODUCT_UPDATED     = "product.updated";
    public static final String PRODUCT_DELETED     = "product.deleted";
    public static final String STOCK_UPDATED       = "product.stock-updated";
    public static final String LOW_STOCK_ALERT     = "product.low-stock-alert";
    public static final String OUT_OF_STOCK        = "product.out-of-stock";
    public static final String BACK_IN_STOCK       = "product.back-in-stock";
    public static final String PRICE_CHANGED       = "product.price-changed";
}