package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.model.request.UpdateOrderStatusRequest;
import com.ecommerce.order_service.model.response.OrderResponse;
import com.ecommerce.order_service.repository.ApiResponse;
import com.ecommerce.order_service.security.JwtUtil;
import com.ecommerce.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderControllerImpl implements AdminOrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            int page, int size, String status, String sortBy, String direction) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched.",
                orderService.getAllOrders(page, size, status, sortBy, direction)));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(String orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order fetched.", orderService.adminGetOrderById(orderId)));
    }

    @Override
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            String orderId, UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest) {

        String adminId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(
                "Order status updated.",
                orderService.updateOrderStatus(orderId, request, adminId)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByUser(
            String userId, int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Orders fetched.",
                orderService.getOrdersByUser(userId, page, size)));
    }

    private String extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(auth.substring(7));
        }
        return null;
    }
}