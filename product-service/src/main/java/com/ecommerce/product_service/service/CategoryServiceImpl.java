package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.enums.ProductStatus;
import com.ecommerce.product_service.exception.CategoryNotFoundException;
import com.ecommerce.product_service.exception.CategoryNotEmptyException;
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
import org.springframework.data.domain.Pageable;
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

    // ── GET CATEGORY TREE ─────────────────────────────────────────────
    @Override
    public List<CategoryResponse> getCategoryTree() {
        log.debug("Fetching full category tree");
        return categoryRepository.findByParentIsNullAndIsActiveTrue()
                .stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    // ── GET CATEGORY BY ID ────────────────────────────────────────────
    @Override
    public CategoryResponse getCategoryById(String id) {
        log.debug("Fetching category by id: {}", id);
        Category category = findCategoryById(id);
        return toCategoryResponse(category);
    }

    // ── GET PRODUCTS BY CATEGORY ──────────────────────────────────────
    @Override
    public Page<ProductResponse> getProductsByCategory(String id, int page, int size) {
        log.debug("Fetching products for category: {}", id);
        findCategoryById(id); // validate exists
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryIdAndStatus(id, ProductStatus.ACTIVE, pageable)
                .map(this::toProductResponse);
    }

    // ── CREATE CATEGORY ───────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();

        if (request.getParentId() != null) {
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} | id: {}", saved.getName(), saved.getId());
        return toCategoryResponse(saved);
    }

    // ── UPDATE CATEGORY ───────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);

        Category category = findCategoryById(id);

        if (request.getName()         != null) category.setName(request.getName());
        if (request.getSlug()         != null) category.setSlug(request.getSlug());
        if (request.getDescription()  != null) category.setDescription(request.getDescription());
        if (request.getImageUrl()     != null) category.setImageUrl(request.getImageUrl());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive()     != null) category.setIsActive(request.getIsActive());
        if (request.getParentId()     != null) {
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated: {}", id);
        return toCategoryResponse(updated);
    }

    // ── DELETE CATEGORY ───────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteCategory(String id) {
        log.info("Deleting category: {}", id);

        Category category = findCategoryById(id);

        // Check if category has products
        long productCount = productRepository
                .findByCategoryIdAndStatus(id, ProductStatus.ACTIVE, PageRequest.of(0, 1))
                .getTotalElements();

        if (productCount > 0) {
            throw new CategoryNotEmptyException(
                    "Cannot delete category with active products. Move or delete products first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted: {}", id);
    }

    // ── REORDER CATEGORY ──────────────────────────────────────────────
    @Override
    @Transactional
    public CategoryResponse reorderCategory(String id, Integer displayOrder) {
        log.info("Reordering category: {} → order: {}", id, displayOrder);

        Category category = findCategoryById(id);
        category.setDisplayOrder(displayOrder);
        Category updated = categoryRepository.save(category);

        log.info("Category reordered: {}", id);
        return toCategoryResponse(updated);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────
    private Category findCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        List<CategoryResponse> children = category.getChildren().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .children(children)
                .build();
    }

    private ProductResponse toProductResponse(Product product) {
        List<String> imageUrls = product.getImages().stream()
                .map(img -> img.getImageUrl()).collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrandName())
                .status(product.getStatus())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .soldCount(product.getSoldCount())
                .imageUrls(imageUrls)
                .inStock(product.getInventory() != null
                        && product.getInventory().getQuantity() > 0)
                .createdAt(product.getCreatedAt())
                .build();
    }
}