package com.ecommerce.user_service.client;

import com.ecommerce.user_service.client.response.ProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products")
    ProductListResponse getProducts(
            @RequestParam(defaultValue = "0")      int page,
            @RequestParam(defaultValue = "20")     int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestHeader("Authorization")        String authToken
    );
}