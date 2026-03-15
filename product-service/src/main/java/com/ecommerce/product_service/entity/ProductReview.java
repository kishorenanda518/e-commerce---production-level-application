package com.ecommerce.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_verified_purchase")
    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "helpful_count")
    @Builder.Default
    private Integer helpfulCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }
}