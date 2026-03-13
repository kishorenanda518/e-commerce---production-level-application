package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserRegisteredEvent {
    private String  userId;
    private String  username;
    private String  email;
    private String  firstName;
    private String  ipAddress;
    private Instant timestamp;
}