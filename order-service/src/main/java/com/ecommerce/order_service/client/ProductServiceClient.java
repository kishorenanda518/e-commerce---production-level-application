package com.ecommerce.order_service.client;


import com.ecommerce.order_service.model.request.ReserveStockRequest;
import com.ecommerce.order_service.model.request.StockOperationRequest;
import com.ecommerce.order_service.model.response.ProductAvailabilityResponse;
import com.ecommerce.order_service.model.response.ProductPriceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/v1/internal/products/{productId}/availability")
    ProductAvailabilityResponse checkAvailability(
            @PathVariable String productId
    );

    @GetMapping("/api/v1/internal/products/{productId}/price")
    ProductPriceResponse getProductPrice(
            @PathVariable String productId
    );

    @PostMapping("/api/v1/internal/products/reserve-stock")
    void reserveStock(@RequestBody ReserveStockRequest request);

    @PostMapping("/api/v1/internal/products/confirm-stock")
    void confirmStock(@RequestBody StockOperationRequest request);

    @PostMapping("/api/v1/internal/products/release-stock")
    void releaseStock(@RequestBody StockOperationRequest request);
}