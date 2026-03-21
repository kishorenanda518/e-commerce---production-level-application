package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

    // ── used by getCategoryTree() ─────────────────────────────
    List<Category> findAllByIsActiveTrueOrderByDisplayOrderAsc();

    // ── top level — parentId is null (NOT parent is null) ─────
    List<Category> findByParentIdIsNullAndIsActiveTrue();

    // ── subcategories by parentId string ─────────────────────
    List<Category> findByParentIdAndIsActiveTrue(String parentId);

    // ── slug check ────────────────────────────────────────────
    boolean existsBySlug(String slug);
}