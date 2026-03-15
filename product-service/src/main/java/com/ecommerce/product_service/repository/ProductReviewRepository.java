// ProductReviewRepository.java
package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {
    Page<ProductReview> findByProductIdAndIsApprovedTrue(String productId, Pageable pageable);
    boolean existsByProductIdAndUserId(String productId, String userId);
    Optional<ProductReview> findByProductIdAndUserId(String productId, String userId);
}