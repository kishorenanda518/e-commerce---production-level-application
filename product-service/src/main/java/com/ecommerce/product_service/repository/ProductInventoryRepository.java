// ProductInventoryRepository.java
package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {
    Optional<ProductInventory> findByProductId(String productId);
}