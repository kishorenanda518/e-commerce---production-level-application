package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.model.request.BulkCreateProductRequest;
import com.ecommerce.product_service.model.request.CreateReviewRequest;
import com.ecommerce.product_service.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Public product APIs")
@RequestMapping("/api/v1/products")
public interface ProductController {

    @Operation(summary = "Get all products paginated")
    @GetMapping
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0")       int page,
            @RequestParam(defaultValue = "20")      int size,
            @RequestParam(defaultValue = "newest")  String sort,
            @RequestParam(required = false)         String categoryId,
            @RequestParam(required = false)         String brand,
            @RequestParam(required = false)         Boolean inStock
    );

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(
            @PathVariable String id
    );

    @Operation(summary = "Get product by SKU")
    @GetMapping("/sku/{sku}")
    ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySku(
            @PathVariable String sku
    );

    @Operation(summary = "Search products")
    @GetMapping("/search")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam(required = false)          String q,
            @RequestParam(required = false)          String categoryId,
            @RequestParam(required = false)          String brand,
            @RequestParam(required = false)          Boolean inStock,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0")        int page,
            @RequestParam(defaultValue = "20")       int size
    );

    @Operation(summary = "Get related products")
    @GetMapping("/{id}/related")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getRelatedProducts(
            @PathVariable String id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "Get product reviews")
    @GetMapping("/{id}/reviews")
    ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "date") String sort
    );

    @Operation(summary = "Submit a product review")
    @PostMapping("/{id}/reviews")
    ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @PathVariable String id,
            @Valid @RequestBody CreateReviewRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get featured products")
    @GetMapping("/featured")
    ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts();

    @Operation(summary = "Get new arrivals")
    @GetMapping("/new-arrivals")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getNewArrivals(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Get best sellers")
    @GetMapping("/best-sellers")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getBestSellers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "Bulk create products")
    @PostMapping("/bulk")
    ResponseEntity<ApiResponse<BulkCreateResult>> bulkCreateProducts(
            @Valid @RequestBody BulkCreateProductRequest request
    );
}