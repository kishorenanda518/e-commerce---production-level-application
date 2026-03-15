package com.ecommerce.product_service.entity;

import com.ecommerce.product_service.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku",      columnList = "sku",         unique = true),
        @Index(name = "idx_product_status",   columnList = "status"),
        @Index(name = "idx_product_category", columnList = "category_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "sold_count")
    @Builder.Default
    private Long soldCount = 0L;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "weight")
    private Double weight;

    // JSON stored as string — tags: ["electronics","laptop"]
    @Column(columnDefinition = "JSON")
    private String tags;

    // JSON stored as string — attributes: {"color":"Red","size":"XL"}
    @Column(columnDefinition = "JSON")
    private String attributes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL,
              orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductInventory inventory;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void generateId() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
    }
}