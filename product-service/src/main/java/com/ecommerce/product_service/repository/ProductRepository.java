package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends
        JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    // ── Fetch with images and inventory eagerly ───────────────────────
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.inventory " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.status = :status")
    Page<Product> findByStatusWithDetails(
            @Param("status") ProductStatus status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.inventory " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.id = :id AND p.status != 'DELETED'")
    Optional<Product> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.inventory " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.sku = :sku AND p.status != :status")
    Optional<Product> findBySkuAndStatusNot(
            @Param("sku") String sku,
            @Param("status") ProductStatus status);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.inventory " +
            "LEFT JOIN FETCH p.category " +
            "WHERE p.category.id = :categoryId AND p.status = :status")
    Page<Product> findByCategoryIdAndStatus(
            @Param("categoryId") String categoryId,
            @Param("status") ProductStatus status,
            Pageable pageable);

    boolean existsBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.inventory i " +
            "WHERE i.quantity <= i.lowStockThreshold AND p.status = 'ACTIVE'")
    Page<Product> findLowStockProducts(Pageable pageable);
}