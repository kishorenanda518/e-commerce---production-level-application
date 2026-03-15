package com.ecommerce.user_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditLogResponse {
    private String  id;           // ← String not Long
    private String  userId;       // ← String not nested User
    private String  action;
    private String  entityType;
    private String  entityId;
    private String  oldValue;
    private String  newValue;
    private String  ipAddress;
    private Instant createdAt;
}