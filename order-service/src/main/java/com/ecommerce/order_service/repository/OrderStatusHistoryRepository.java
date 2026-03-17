// OrderStatusHistoryRepository.java
package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.entites.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtDesc(String orderId);
}