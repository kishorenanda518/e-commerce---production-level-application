package com.ecommerce.order_service.config;

import com.ecommerce.order_service.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean public NewTopic productCreated()    { return TopicBuilder.name(KafkaTopics.ORDER_CONFIRMED).partitions(3).replicas(1).build(); }

}