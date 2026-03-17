package com.ecommerce.product_service.client;

import com.ecommerce.product_service.model.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/internal/users/{userId}")
    UserResponse getUserById(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authToken
    );
}