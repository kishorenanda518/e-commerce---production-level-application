package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.request.CancelOrderRequest;
import com.ecommerce.order_service.model.request.CreateOrderRequest;
import com.ecommerce.order_service.model.response.OrderResponse;
import com.ecommerce.order_service.model.response.OrderStatusHistoryResponse;
import com.ecommerce.order_service.repository.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Customer order APIs")
@RequestMapping("/api/v1/orders")
public interface OrderController {

    @Operation(summary = "Place a new order")
    @PostMapping
    ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable String orderId,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get order by order number")
    @GetMapping("/number/{orderNumber}")
    ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get my orders")
    @GetMapping("/my-orders")
    ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String status,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Cancel an order")
    @PatchMapping("/{orderId}/cancel")
    ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get order status history")
    @GetMapping("/{orderId}/history")
    ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getOrderHistory(
            @PathVariable String orderId,
            HttpServletRequest httpRequest
    );
}