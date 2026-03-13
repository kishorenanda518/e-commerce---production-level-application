package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserLoginEvent {
    private String  userId;
    private String  username;
    private String  email;
    private String  ipAddress;
    private String  userAgent;
    private Instant timestamp;
}