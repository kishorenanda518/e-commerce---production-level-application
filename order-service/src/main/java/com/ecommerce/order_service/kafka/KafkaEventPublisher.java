package com.ecommerce.order_service.kafka;

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
        log.info("Publishing event → topic: {} | key: {}", topic, key);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish → topic: {} | error: {}", topic, ex.getMessage());
            } else {
                log.info("Published → topic: {} | offset: {}",
                        topic, result.getRecordMetadata().offset());
            }
        });
    }
}