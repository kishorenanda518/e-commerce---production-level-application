package com.ecommerce.order_service.service;


import com.ecommerce.order_service.entites.Order;
import com.ecommerce.order_service.entites.OrderItem;
import com.ecommerce.order_service.entites.OrderStatusHistory;
import com.ecommerce.order_service.enums.OrderStatus;
import com.ecommerce.order_service.enums.PaymentStatus;
import com.ecommerce.order_service.exception.InsufficientStockException;
import com.ecommerce.order_service.exception.OrderNotFoundException;
import com.ecommerce.order_service.exception.OrderStatusException;
import com.ecommerce.order_service.kafka.KafkaEventPublisher;
import com.ecommerce.order_service.kafka.KafkaTopics;
import com.ecommerce.order_service.kafka.event.OrderPlacedEvent;
import com.ecommerce.order_service.kafka.event.OrderStatusChangedEvent;
import com.ecommerce.order_service.model.request.*;
import com.ecommerce.order_service.model.response.*;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.repository.OrderStatusHistoryRepository;
import com.ecommerce.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.order_service.client.ProductServiceClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository              orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ProductServiceClient productServiceClient;
    private final KafkaEventPublisher          kafkaEventPublisher;

    // ── CREATE ORDER ──────────────────────────────────────────────
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,
                                     String userId, String authToken) {

        log.info("Creating order for userId: {} | items: {}",
                userId, request.getItems().size());

        // Step 1: Validate all products and calculate totals
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductAvailabilityResponse availability =
                    productServiceClient.checkAvailability(itemRequest.getProductId());

            if (!availability.isAvailable()) {
                throw new InsufficientStockException(
                        "Product '" + availability.getName() + "' is out of stock.");
            }

            if (availability.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Only " + availability.getStock() + " units available for '"
                        + availability.getName() + "'.");
            }

            BigDecimal unitPrice  = availability.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(totalPrice);

            orderItems.add(OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(availability.getName())
                    .productSku(availability.getSku())
                    .productImage(availability.getPrimaryImageUrl())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build());
        }

        // Step 2: Calculate shipping and tax
        BigDecimal shippingAmount = subtotal.compareTo(
                BigDecimal.valueOf(999)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(99);

        BigDecimal taxAmount = subtotal
                .multiply(BigDecimal.valueOf(0.18))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        BigDecimal totalAmount = subtotal
                .add(shippingAmount)
                .add(taxAmount);

        // Step 3: Build order
        var addr = request.getShippingAddress();
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .subtotal(subtotal)
                .shippingAmount(shippingAmount)
                .taxAmount(taxAmount)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .couponCode(request.getCouponCode())
                .shippingName(addr.getName())
                .shippingPhone(addr.getPhone())
                .shippingStreet(addr.getStreet())
                .shippingCity(addr.getCity())
                .shippingState(addr.getState())
                .shippingCountry(addr.getCountry())
                .shippingPostalCode(addr.getPostalCode())
                .estimatedDelivery(Instant.now().plus(7, ChronoUnit.DAYS))
                .notes(request.getNotes())
                .build();

        // Step 4: Link items to order
        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        // Step 5: Reserve stock for each item
        for (OrderItem item : orderItems) {
            try {
                productServiceClient.reserveStock(
                        ReserveStockRequest.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .orderId(saved.getId())
                                .build());
            } catch (Exception e) {
                log.warn("Failed to reserve stock for product: {} | {}",
                        item.getProductId(), e.getMessage());
            }
        }

        // Step 6: Save status history
        saveStatusHistory(saved.getId(), null, OrderStatus.PENDING,
                userId, "Order placed");

        // Step 7: Publish Kafka event
        kafkaEventPublisher.publish(
                KafkaTopics.ORDER_PLACED,
                saved.getId(),
                OrderPlacedEvent.builder()
                        .orderId(saved.getId())
                        .orderNumber(saved.getOrderNumber())
                        .userId(userId)
                        .totalAmount(totalAmount)
                        .itemCount(orderItems.size())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Order created: {} | total: {}", saved.getOrderNumber(), totalAmount);
        return toOrderResponse(saved);
    }

    // ── GET ORDER BY ID ───────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, String userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }
        return toOrderResponse(order);
    }

    // ── GET ORDER BY NUMBER ───────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber, String userId) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderNumber));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found: " + orderNumber);
        }
        return toOrderResponse(order);
    }

    // ── GET MY ORDERS ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String userId, int page,
                                            int size, String status) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable)
                    .map(this::toOrderResponse);
        }
        return orderRepository.findByUserId(userId, pageable)
                .map(this::toOrderResponse);
    }

    // ── CANCEL ORDER ──────────────────────────────────────────────
    @Override
    @Transactional
    public void cancelOrder(String orderId, String userId, CancelOrderRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderStatusException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Release stock
        for (OrderItem item : order.getItems()) {
            try {
                productServiceClient.releaseStock(
                        StockOperationRequest.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .orderId(orderId)
                                .reason(request.getReason())
                                .build());
            } catch (Exception e) {
                log.warn("Failed to release stock: {}", e.getMessage());
            }
        }

        saveStatusHistory(orderId, oldStatus, OrderStatus.CANCELLED,
                userId, request.getReason());

        kafkaEventPublisher.publish(
                KafkaTopics.ORDER_CANCELLED,
                orderId,
                OrderStatusChangedEvent.builder()
                        .orderId(orderId)
                        .orderNumber(order.getOrderNumber())
                        .userId(userId)
                        .oldStatus(oldStatus.name())
                        .newStatus(OrderStatus.CANCELLED.name())
                        .reason(request.getReason())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Order cancelled: {}", order.getOrderNumber());
    }

    // ── GET ORDER HISTORY ─────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getOrderHistory(
            String orderId, String userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        return historyRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ── ADMIN: GET ALL ORDERS ─────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size, String status,
                                            String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus, pageable)
                    .map(this::toOrderResponse);
        }
        return orderRepository.findAll(pageable)
                .map(this::toOrderResponse);
    }

    // ── ADMIN: UPDATE ORDER STATUS ────────────────────────────────
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId,
                                            UpdateOrderStatusRequest request,
                                            String adminId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(Instant.now());
        }

        orderRepository.save(order);
        saveStatusHistory(orderId, oldStatus, request.getStatus(),
                adminId, request.getReason());

        kafkaEventPublisher.publish(
                KafkaTopics.ORDER_CONFIRMED,
                orderId,
                OrderStatusChangedEvent.builder()
                        .orderId(orderId)
                        .orderNumber(order.getOrderNumber())
                        .userId(order.getUserId())
                        .oldStatus(oldStatus.name())
                        .newStatus(request.getStatus().name())
                        .reason(request.getReason())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Order status updated: {} → {}", order.getOrderNumber(), request.getStatus());
        return toOrderResponse(order);
    }

    // ── ADMIN: GET ORDERS BY USER ─────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return orderRepository.findByUserId(userId, pageable)
                .map(this::toOrderResponse);
    }

    // ── ADMIN: GET ORDER BY ID ────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public OrderResponse adminGetOrderById(String orderId) {
        return toOrderResponse(
                orderRepository.findByIdWithItems(orderId)
                        .orElseThrow(() -> new OrderNotFoundException(
                                "Order not found: " + orderId)));
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────
    private String generateOrderNumber() {
        String prefix = "ORD";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = String.format("%04d", new Random().nextInt(9999));
        return prefix + timestamp + random;
    }

    private void saveStatusHistory(String orderId, OrderStatus oldStatus,
                                    OrderStatus newStatus, String changedBy,
                                    String reason) {
        historyRepository.save(OrderStatusHistory.builder()
                .orderId(orderId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .build());
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems() == null
                ? new ArrayList<>()
                : order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productSku(item.getProductSku())
                                .productImage(item.getProductImage())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingAmount(order.getShippingAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .items(items)
                .shippingAddress(ShippingAddressResponse.builder()
                        .name(order.getShippingName())
                        .phone(order.getShippingPhone())
                        .street(order.getShippingStreet())
                        .city(order.getShippingCity())
                        .state(order.getShippingState())
                        .country(order.getShippingCountry())
                        .postalCode(order.getShippingPostalCode())
                        .build())
                .trackingNumber(order.getTrackingNumber())
                .estimatedDelivery(order.getEstimatedDelivery())
                .deliveredAt(order.getDeliveredAt())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory h) {
        return OrderStatusHistoryResponse.builder()
                .id(h.getId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .changedBy(h.getChangedBy())
                .reason(h.getReason())
                .createdAt(h.getCreatedAt())
                .build();
    }
}