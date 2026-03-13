package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserPasswordChangedEvent {
    private String  userId;
    private String  ipAddress;
    private Instant timestamp;
}