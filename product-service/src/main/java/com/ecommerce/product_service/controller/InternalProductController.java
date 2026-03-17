package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.response.ProductAvailabilityResponse;
import com.ecommerce.product_service.model.response.ProductPriceResponse;
import com.ecommerce.product_service.model.request.StockOperationRequest;
import com.ecommerce.product_service.model.request.ReserveStockRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Internal", description = "Internal APIs for service-to-service communication")
@RequestMapping("/api/v1/internal/products")
public interface InternalProductController {

    @Operation(summary = "Check product availability")
    @GetMapping("/{productId}/availability")
    ResponseEntity<ProductAvailabilityResponse> checkAvailability(
            @PathVariable String productId
    );

    @Operation(summary = "Get product price")
    @GetMapping("/{productId}/price")
    ResponseEntity<ProductPriceResponse> getProductPrice(
            @PathVariable String productId
    );

    @Operation(summary = "Reserve stock for order")
    @PostMapping("/reserve-stock")
    ResponseEntity<Void> reserveStock(
            @RequestBody ReserveStockRequest request
    );

    @Operation(summary = "Confirm stock deduction")
    @PostMapping("/confirm-stock")
    ResponseEntity<Void> confirmStock(
            @RequestBody StockOperationRequest request
    );

    @Operation(summary = "Release reserved stock")
    @PostMapping("/release-stock")
    ResponseEntity<Void> releaseStock(
            @RequestBody StockOperationRequest request
    );
}