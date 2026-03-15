package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.*;
import com.ecommerce.product_service.model.response.*;
import com.ecommerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class AdminProductControllerImpl implements AdminProductController {

    private final ProductService productService;

    @Override
    public ResponseEntity<ApiResponse<Page<ProductDetailResponse>>> getAllProducts(
            int page, int size, String sortBy, String direction) {
        return ResponseEntity.ok(ApiResponse.success(
                "Products fetched successfully.",
                productService.adminGetAllProducts(page, size, sortBy, direction)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Product created successfully.",
                        productService.createProduct(request)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product fetched successfully.",
                productService.getProductById(id)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            String id, UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product updated successfully.",
                productService.updateProduct(id, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> patchProduct(
            String id, UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product updated successfully.",
                productService.patchProduct(id, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteProduct(String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Product deleted successfully.", null));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProductStatus(
            String id, UpdateProductStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product status updated.",
                productService.updateProductStatus(id, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            String id, List<MultipartFile> files) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Images uploaded successfully.",
                        productService.uploadImages(id, files)));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteImage(String id, String imageId) {
        productService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success(
                "Image deleted successfully.", null));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateInventory(
            String id, UpdateInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory updated successfully.",
                productService.updateInventory(id, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getLowStockProducts(
            int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Low stock products fetched.",
                productService.getLowStockProducts(page, size)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductStatsResponse>> getProductStatistics() {
        return ResponseEntity.ok(ApiResponse.success(
                "Statistics fetched.",
                productService.getProductStatistics()));
    }
}