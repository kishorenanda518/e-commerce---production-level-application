// StockMovementRepository.java
package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(String productId);
}