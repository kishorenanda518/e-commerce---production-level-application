package com.ecommerce.user_service.kafka.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class UserRoleEvent {
    private String  userId;
    private String  roleName;
    private String  assignedBy;
    private Instant timestamp;
}