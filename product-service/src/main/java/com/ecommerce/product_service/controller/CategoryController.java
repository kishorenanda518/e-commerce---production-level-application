package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.CreateCategoryRequest;
import com.ecommerce.product_service.model.request.UpdateCategoryRequest;
import com.ecommerce.product_service.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Category management APIs")
@RequestMapping("/api/v1/categories")
public interface CategoryController {

    @Operation(summary = "Get full category tree")
    @GetMapping
    ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree();

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable String id
    );

    @Operation(summary = "Get products in category")
    @GetMapping("/{id}/products")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getProductsByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Create category")
    @PostMapping("/admin")
    ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    );

    @Operation(summary = "Update category")
    @PutMapping("/admin/{id}")
    ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request
    );

    @Operation(summary = "Delete category")
    @DeleteMapping("/admin/{id}")
    ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable String id
    );

    @Operation(summary = "Reorder category")
    @PatchMapping("/admin/{id}/reorder")
    ResponseEntity<ApiResponse<CategoryResponse>> reorderCategory(
            @PathVariable String id,
            @RequestParam Integer displayOrder
    );
}