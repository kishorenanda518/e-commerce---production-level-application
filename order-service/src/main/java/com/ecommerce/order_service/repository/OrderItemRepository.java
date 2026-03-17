// OrderItemRepository.java
package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.entites.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrderId(String orderId);
}