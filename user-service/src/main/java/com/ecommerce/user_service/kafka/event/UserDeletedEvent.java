package com.ecommerce.user_service.kafka.event;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class UserDeletedEvent {
    private String userId;
    private String email;
    private String reason;
    private Instant timestamp;
}
