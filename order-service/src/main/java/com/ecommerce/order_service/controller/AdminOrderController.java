package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.request.UpdateOrderStatusRequest;
import com.ecommerce.order_service.model.response.OrderResponse;
import com.ecommerce.order_service.repository.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Orders", description = "Admin order management APIs")
@RequestMapping("/api/v1/admin/orders")
public interface AdminOrderController {

    @Operation(summary = "Get all orders")
    @GetMapping
    ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size,
            @RequestParam(required = false)            String status,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String direction
    );

    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable String orderId
    );

    @Operation(summary = "Update order status")
    @PatchMapping("/{orderId}/status")
    ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get orders by user")
    @GetMapping("/user/{userId}")
    ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    );
}