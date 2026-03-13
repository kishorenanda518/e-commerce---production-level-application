package com.ecommerce.user_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic: {} | key: {} | error: {}",
                        topic, key, ex.getMessage());
            } else {
                log.info("Event published → topic: {} | key: {} | offset: {}",
                        topic, key,
                        result.getRecordMetadata().offset());
            }
        });
    }
}