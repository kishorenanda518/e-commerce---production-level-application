package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.entity.ProductInventory;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.model.request.ReserveStockRequest;
import com.ecommerce.product_service.model.request.StockOperationRequest;
import com.ecommerce.product_service.model.response.ProductAvailabilityResponse;
import com.ecommerce.product_service.model.response.ProductPriceResponse;
import com.ecommerce.product_service.repository.ProductInventoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/products")
public class InternalProductControllerImpl implements InternalProductController {

    private final ProductRepository          productRepository;
    private final ProductInventoryRepository inventoryRepository;

    // ── CHECK AVAILABILITY ────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)          // ← ADD
    public ResponseEntity<ProductAvailabilityResponse> checkAvailability(
            String productId) {

        log.info("Internal: check availability for product: {}", productId);

        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found: " + productId));

        ProductInventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElse(null);

        int stock     = inventory != null ? inventory.getQuantity() : 0;
        int reserved  = inventory != null ? inventory.getReservedQuantity() : 0;
        int available = stock - reserved;

        // ── safe image access inside transaction ──────────────────
        String primaryImage = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImage = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(img -> img.getImageUrl())
                    .findFirst()
                    .orElse(product.getImages().get(0).getImageUrl());
        }

        return ResponseEntity.ok(ProductAvailabilityResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .available(available > 0)
                .stock(available)
                .price(product.getPrice())
                .primaryImageUrl(primaryImage)
                .build());
    }

    // ── GET PRICE ─────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)          // ← ADD
    public ResponseEntity<ProductPriceResponse> getProductPrice(String productId) {

        log.info("Internal: get price for product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found: " + productId));

        return ResponseEntity.ok(ProductPriceResponse.builder()
                .productId(product.getId())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .build());
    }

    // ── RESERVE STOCK ─────────────────────────────────────────────
    @Override
    @Transactional                           // ← ADD
    public ResponseEntity<Void> reserveStock(ReserveStockRequest request) {

        log.info("Internal: reserve stock — product: {} | qty: {} | order: {}",
                request.getProductId(), request.getQuantity(), request.getOrderId());

        ProductInventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Inventory not found: " + request.getProductId()));

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + request.getQuantity());
        inventoryRepository.save(inventory);

        log.info("Stock reserved — product: {} | reserved: {}",
                request.getProductId(), inventory.getReservedQuantity());

        return ResponseEntity.ok().build();
    }

    // ── CONFIRM STOCK ─────────────────────────────────────────────
    @Override
    @Transactional                           // ← ADD
    public ResponseEntity<Void> confirmStock(StockOperationRequest request) {

        log.info("Internal: confirm stock — product: {} | qty: {}",
                request.getProductId(), request.getQuantity());

        ProductInventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Inventory not found: " + request.getProductId()));

        inventory.setQuantity(
                inventory.getQuantity() - request.getQuantity());
        inventory.setReservedQuantity(
                Math.max(0, inventory.getReservedQuantity() - request.getQuantity()));
        inventoryRepository.save(inventory);

        log.info("Stock confirmed — product: {} | remaining: {}",
                request.getProductId(), inventory.getQuantity());

        return ResponseEntity.ok().build();
    }

    // ── RELEASE STOCK ─────────────────────────────────────────────
    @Override
    @Transactional                           // ← ADD
    public ResponseEntity<Void> releaseStock(StockOperationRequest request) {

        log.info("Internal: release stock — product: {} | qty: {}",
                request.getProductId(), request.getQuantity());

        ProductInventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        "Inventory not found: " + request.getProductId()));

        inventory.setReservedQuantity(
                Math.max(0, inventory.getReservedQuantity() - request.getQuantity()));
        inventoryRepository.save(inventory);

        log.info("Stock released — product: {} | reserved now: {}",
                request.getProductId(), inventory.getReservedQuantity());

        return ResponseEntity.ok().build();
    }
}