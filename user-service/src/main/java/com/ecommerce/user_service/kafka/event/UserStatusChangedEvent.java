package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class UserStatusChangedEvent {
    private String userId;
    private String oldStatus;
    private String newStatus;
    private String changedBy;
    private String reason;
    private Instant timestamp;
}
