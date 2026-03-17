package com.ecommerce.order_service.config;

import com.ecommerce.product_service.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean public NewTopic productCreated()    { return TopicBuilder.name(KafkaTopics.PRODUCT_CREATED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic productUpdated()    { return TopicBuilder.name(KafkaTopics.PRODUCT_UPDATED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic productDeleted()    { return TopicBuilder.name(KafkaTopics.PRODUCT_DELETED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic stockUpdated()      { return TopicBuilder.name(KafkaTopics.STOCK_UPDATED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic lowStockAlert()     { return TopicBuilder.name(KafkaTopics.LOW_STOCK_ALERT).partitions(3).replicas(1).build(); }
    @Bean public NewTopic outOfStock()        { return TopicBuilder.name(KafkaTopics.OUT_OF_STOCK).partitions(3).replicas(1).build(); }
    @Bean public NewTopic backInStock()       { return TopicBuilder.name(KafkaTopics.BACK_IN_STOCK).partitions(3).replicas(1).build(); }
    @Bean public NewTopic priceChanged()      { return TopicBuilder.name(KafkaTopics.PRICE_CHANGED).partitions(3).replicas(1).build(); }
}