package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.enums.ProductStatus;
import com.ecommerce.product_service.exception.ResourceNotFoundException;
import com.ecommerce.product_service.model.request.CreateCategoryRequest;
import com.ecommerce.product_service.model.request.UpdateCategoryRequest;
import com.ecommerce.product_service.model.response.CategoryResponse;
import com.ecommerce.product_service.model.response.ProductResponse;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;

    // ── GET ALL ───────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository
                .findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String id) {
        return toResponse(
                categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Category", "id", id))
        );
    }

    // ── GET PRODUCTS BY CATEGORY ──────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String id, int page, int size) {
        return productRepository
                .findByCategoryIdAndStatus(id, ProductStatus.ACTIVE, PageRequest.of(page, size))
                .map(this::toProductResponse);
    }

    // ── CREATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .imageUrl(request.getImageUrl())
                .build();
        return toResponse(categoryRepository.save(category));
    }

    // ── UPDATE ────────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (request.getName()         != null) category.setName(request.getName());
        if (request.getSlug()         != null) category.setSlug(request.getSlug());
        if (request.getDescription()  != null) category.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive()     != null) category.setIsActive(request.getIsActive());

        return toResponse(categoryRepository.save(category));
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Category", "id", id);
        categoryRepository.deleteById(id);
    }

    // ── REORDER ───────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse reorderCategory(String id, Integer displayOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setDisplayOrder(displayOrder);
        return toResponse(categoryRepository.save(category));
    }

    // ── MAPPER ───────────────────────────────────────────────
    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentId(c.getParentId())        // ← plain String, no lazy
                .displayOrder(c.getDisplayOrder())
                .isActive(c.getIsActive())
                .imageUrl(c.getImageUrl())
                .build();
    }

    private ProductResponse toProductResponse(
            com.ecommerce.product_service.entity.Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .build();
    }
}