package com.ecommerce.order_service.service;

import com.ecommerce.order_service.model.request.CancelOrderRequest;
import com.ecommerce.order_service.model.request.CreateOrderRequest;
import com.ecommerce.order_service.model.request.UpdateOrderStatusRequest;
import com.ecommerce.order_service.model.response.OrderResponse;
import com.ecommerce.order_service.model.response.OrderStatusHistoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    // ── Customer APIs ─────────────────────────────────────────────
    OrderResponse           createOrder(CreateOrderRequest request, String userId, String authToken);
    OrderResponse           getOrderById(String orderId, String userId);
    OrderResponse           getOrderByNumber(String orderNumber, String userId);
    Page<OrderResponse>     getMyOrders(String userId, int page, int size, String status);
    void                    cancelOrder(String orderId, String userId, CancelOrderRequest request);
    List<OrderStatusHistoryResponse> getOrderHistory(String orderId, String userId);

    // ── Admin APIs ────────────────────────────────────────────────
    Page<OrderResponse>     getAllOrders(int page, int size, String status, String sortBy, String direction);
    OrderResponse           updateOrderStatus(String orderId, UpdateOrderStatusRequest request, String adminId);
    Page<OrderResponse>     getOrdersByUser(String userId, int page, int size);
    OrderResponse           adminGetOrderById(String orderId);
}