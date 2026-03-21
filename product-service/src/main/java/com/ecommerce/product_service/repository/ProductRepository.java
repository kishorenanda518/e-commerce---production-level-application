package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    // ── MAIN DYNAMIC FILTER QUERY ─────────────────────────────
    // Entity fields used:
    //   Product.status, Product.category.id, Product.brandName,
    //   Product.price, Product.name, Product.inventory.quantity
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images    img
        LEFT JOIN FETCH p.inventory inv
        LEFT JOIN FETCH p.category  cat
        WHERE p.status = :status
          AND (:categoryId IS NULL OR p.category.id   = :categoryId)
          AND (:brand      IS NULL OR LOWER(p.brandName) = LOWER(:brand))
          AND (:minPrice   IS NULL OR p.price         >= :minPrice)
          AND (:maxPrice   IS NULL OR p.price         <= :maxPrice)
          AND (
               :q IS NULL
               OR LOWER(p.name)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.brandName) LIKE LOWER(CONCAT('%', :q, '%'))
              )
          AND (
               :inStock IS NULL
               OR (:inStock = TRUE  AND p.inventory.quantity > 0)
               OR (:inStock = FALSE AND p.inventory.quantity = 0)
              )
        """)
    Page<Product> findWithFilters(
            @Param("status")     ProductStatus status,
            @Param("categoryId") String        categoryId,
            @Param("brand")      String        brand,
            @Param("minPrice")   BigDecimal    minPrice,
            @Param("maxPrice")   BigDecimal    maxPrice,
            @Param("q")          String        q,
            @Param("inStock")    Boolean       inStock,
            Pageable pageable
    );

    // ── ALL ACTIVE WITH DETAILS (no filters) ──────────────────
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.inventory
        LEFT JOIN FETCH p.category
        WHERE p.status = :status
        """)
    Page<Product> findByStatusWithDetails(
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    // ── BY ID WITH DETAILS ────────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.inventory
        LEFT JOIN FETCH p.category
        WHERE p.id = :id
        """)
    Optional<Product> findByIdWithDetails(@Param("id") String id);

    // ── BY CATEGORY AND STATUS ────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.inventory
        LEFT JOIN FETCH p.category
        WHERE p.category.id = :categoryId
          AND p.status = :status
        """)
    Page<Product> findByCategoryIdAndStatus(
            @Param("categoryId") String        categoryId,
            @Param("status")     ProductStatus status,
            Pageable pageable
    );

    // ── BY SKU ────────────────────────────────────────────────
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.inventory
        LEFT JOIN FETCH p.category
        WHERE p.sku = :sku AND p.status != :status
        """)
    Optional<Product> findBySkuAndStatusNot(
            @Param("sku")    String        sku,
            @Param("status") ProductStatus status
    );

    // ── LOW STOCK ─────────────────────────────────────────────
    // inventory.quantity → ProductInventory.quantity (field name)
    // inventory.lowStockThreshold → ProductInventory.lowStockThreshold
    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.inventory inv
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.category
        WHERE p.status = 'ACTIVE'
          AND p.inventory.quantity <= p.inventory.lowStockThreshold
          AND p.inventory.quantity > 0
        """)
    Page<Product> findLowStockProducts(Pageable pageable);

    // ── STATUS FILTER ─────────────────────────────────────────
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // ── SKU EXISTS ────────────────────────────────────────────
    boolean existsBySku(String sku);

    @Query(
            value = """
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.inventory
        LEFT JOIN FETCH p.category
        WHERE p.status = :status
          AND p.category.id IN :categoryIds
        """,
            countQuery = """
        SELECT COUNT(p) FROM Product p
        WHERE p.status = :status
          AND p.category.id IN :categoryIds
        """
    )
    Page<Product> findByStatusAndCategoryIdIn(
            @Param("status")      ProductStatus  status,
            @Param("categoryIds") List<String> categoryIds,
            Pageable              pageable
    );
}