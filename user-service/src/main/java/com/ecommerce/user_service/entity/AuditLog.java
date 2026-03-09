package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    // Not a FK — just stores the userId string for audit history
    // even if user is deleted later
    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(nullable = false, length = 100)
    private String action;           // e.g. "USER_REGISTERED", "USER_UPDATED"

    @Column(name = "entity_type", length = 50)
    private String entityType;       // e.g. "User", "Address"

    @Column(name = "entity_id", length = 36)
    private String entityId;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;         // JSON string of old state

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;         // JSON string of new state

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}