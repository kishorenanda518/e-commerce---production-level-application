package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.CreateCategoryRequest;
import com.ecommerce.product_service.model.request.UpdateCategoryRequest;
import com.ecommerce.product_service.model.response.*;
import com.ecommerce.product_service.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        return ResponseEntity.ok(ApiResponse.success(
                "Category tree fetched.",
                categoryService.getCategoryTree()));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Category fetched.",
                categoryService.getCategoryById(id)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            String id, int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Products fetched.",
                categoryService.getProductsByCategory(id, page, size)));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            CreateCategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully.",
                        categoryService.createCategory(request)));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            String id, UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Category updated successfully.",
                categoryService.updateCategory(id, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteCategory(String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Category deleted successfully.", null));
    }

    @Override
    public ResponseEntity<ApiResponse<CategoryResponse>> reorderCategory(
            String id, Integer displayOrder) {
        return ResponseEntity.ok(ApiResponse.success(
                "Category reordered.",
                categoryService.reorderCategory(id, displayOrder)));
    }
}