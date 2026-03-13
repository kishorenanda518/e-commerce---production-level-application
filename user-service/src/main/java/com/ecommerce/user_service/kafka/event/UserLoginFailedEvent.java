package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserLoginFailedEvent {
    private String  attemptedUsername;
    private String  ipAddress;
    private String  reason;
    private Instant timestamp;
}