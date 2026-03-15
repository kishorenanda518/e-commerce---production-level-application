package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
public class UserUpdatedEvent {
    private String userId;
    private List<String> updatedFields;
    private Instant timestamp;
}
