package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.request.CancelOrderRequest;
import com.ecommerce.order_service.model.request.CreateOrderRequest;
import com.ecommerce.order_service.model.response.OrderResponse;
import com.ecommerce.order_service.model.response.OrderStatusHistoryResponse;
import com.ecommerce.order_service.repository.ApiResponse;
import com.ecommerce.order_service.security.JwtUtil;
import com.ecommerce.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            CreateOrderRequest request, HttpServletRequest httpRequest) {

        String userId    = extractUserId(httpRequest);
        String authToken = extractAuthToken(httpRequest);

        OrderResponse order = orderService.createOrder(request, userId, authToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully.", order));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            String orderId, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched.", orderService.getOrderById(orderId, userId)));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            String orderNumber, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched.", orderService.getOrderByNumber(orderNumber, userId)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            int page, int size, String status, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched.", orderService.getMyOrders(userId, page, size, status)));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            String orderId, CancelOrderRequest request, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        orderService.cancelOrder(orderId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled.", null));
    }

    @Override
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getOrderHistory(
            String orderId, HttpServletRequest httpRequest) {
        String userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Order history fetched.",
                orderService.getOrderHistory(orderId, userId)));
    }

    private String extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(auth.substring(7));
        }
        return null;
    }

    private String extractAuthToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}