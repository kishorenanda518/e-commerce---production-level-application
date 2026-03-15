// CategoryRepository.java
package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Category> findByParentIsNullAndIsActiveTrue();
    List<Category> findByParentIdAndIsActiveTrue(String parentId);
}