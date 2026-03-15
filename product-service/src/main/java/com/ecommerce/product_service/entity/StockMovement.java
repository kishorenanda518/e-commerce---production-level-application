package com.ecommerce.product_service.entity;

import com.ecommerce.product_service.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }
}