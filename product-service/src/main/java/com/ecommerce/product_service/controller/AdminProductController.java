package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.*;
import com.ecommerce.product_service.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Admin Products", description = "Admin product management APIs")
@RequestMapping("/api/v1/admin/products")
public interface AdminProductController {

    @Operation(summary = "List all products")
    @GetMapping
    ResponseEntity<ApiResponse<Page<ProductDetailResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc")      String direction
    );

    @Operation(summary = "Create product")
    @PostMapping
    ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request
    );

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            @PathVariable String id
    );

    @Operation(summary = "Full update product")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request
    );

    @Operation(summary = "Partial update product")
    @PatchMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDetailResponse>> patchProduct(
            @PathVariable String id,
            @RequestBody UpdateProductRequest request
    );

    @Operation(summary = "Delete product")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id
    );

    @Operation(summary = "Update product status")
    @PatchMapping("/{id}/status")
    ResponseEntity<ApiResponse<ProductDetailResponse>> updateProductStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductStatusRequest request
    );

    @Operation(summary = "Upload product images")
    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            @PathVariable String id,
            @RequestParam("files") List<MultipartFile> files
    );

    @Operation(summary = "Delete product image")
    @DeleteMapping("/{id}/images/{imageId}")
    ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable String id,
            @PathVariable String imageId
    );

    @Operation(summary = "Update inventory")
    @PatchMapping("/{id}/inventory")
    ResponseEntity<ApiResponse<ProductDetailResponse>> updateInventory(
            @PathVariable String id,
            @Valid @RequestBody UpdateInventoryRequest request
    );

    @Operation(summary = "Get low stock products")
    @GetMapping("/low-stock")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get product statistics")
    @GetMapping("/statistics")
    ResponseEntity<ApiResponse<ProductStatsResponse>> getProductStatistics();
}