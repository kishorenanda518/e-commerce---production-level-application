package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.BulkCreateProductRequest;
import com.ecommerce.product_service.model.request.CreateReviewRequest;
import com.ecommerce.product_service.model.request.ProductFilterRequest;
import com.ecommerce.product_service.model.response.*;
import com.ecommerce.product_service.security.JwtUtil;
import com.ecommerce.product_service.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/products")
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;
    private final JwtUtil        jwtUtil;

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            int page, int size, String sort, String categoryId,
            String brand, Boolean inStock) {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setPage(page);
        filter.setSize(size);
        filter.setSort(sort);
        filter.setCategoryId(categoryId);
        filter.setBrand(brand);
        filter.setInStock(inStock);

        return ResponseEntity.ok(ApiResponse.success(
                "Products fetched successfully.",
                productService.getAllProducts(filter)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(String id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product fetched successfully.",
                productService.getProductById(id)));
    }

    @Override
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySku(String sku) {
        return ResponseEntity.ok(ApiResponse.success(
                "Product fetched successfully.",
                productService.getProductBySku(sku)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            String q, String categoryId, String brand,
            Boolean inStock, String sort, int page, int size) {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setQ(q);
        filter.setCategoryId(categoryId);
        filter.setBrand(brand);
        filter.setInStock(inStock);
        filter.setSort(sort);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(ApiResponse.success(
                "Search results fetched.",
                productService.searchProducts(filter)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getRelatedProducts(
            String id, int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Related products fetched.",
                productService.getRelatedProducts(id, page, size)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            String id, int page, int size, String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                "Reviews fetched.",
                productService.getProductReviews(id, page, size, sort)));
    }

    @Override
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            String id, CreateReviewRequest request, HttpServletRequest httpRequest) {

        String userId = extractUserId(httpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Review submitted successfully.",
                        productService.submitReview(id, userId, request)));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                "Featured products fetched.",
                productService.getFeaturedProducts()));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "New arrivals fetched.",
                productService.getNewArrivals(page, size)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getBestSellers(
            int page, int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Best sellers fetched.",
                productService.getBestSellers(page, size)));
    }

    @Override
    public ResponseEntity<ApiResponse<BulkCreateResult>> bulkCreateProducts(
            BulkCreateProductRequest request) {

        log.info("Bulk product create request — count: {}",
                request.getProducts().size());

        BulkCreateResult result = productService.bulkCreateProducts(request);

        String message = String.format(
                "Bulk create complete. Success: %d | Failed: %d",
                result.getTotalSuccess(), result.getTotalFailed());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, result));
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────
    private String extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(auth.substring(7));
        }
        return null;
    }
}