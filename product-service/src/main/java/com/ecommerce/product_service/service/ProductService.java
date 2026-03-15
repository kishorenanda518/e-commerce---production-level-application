package com.ecommerce.product_service.service;

import com.ecommerce.product_service.model.request.*;
import com.ecommerce.product_service.model.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    // ── Public ────────────────────────────────────────────────────
    Page<ProductResponse>       getAllProducts(ProductFilterRequest filter);
    ProductDetailResponse       getProductById(String id);
    ProductDetailResponse       getProductBySku(String sku);
    Page<ProductResponse>       searchProducts(ProductFilterRequest filter);
    Page<ProductResponse>       getRelatedProducts(String id, int page, int size);
    Page<ReviewResponse>        getProductReviews(String id, int page, int size, String sort);
    ReviewResponse              submitReview(String productId, String userId, CreateReviewRequest request);
    List<ProductResponse>       getFeaturedProducts();
    Page<ProductResponse>       getNewArrivals(int page, int size);
    Page<ProductResponse>       getBestSellers(int page, int size);

    // ── Admin ─────────────────────────────────────────────────────
    Page<ProductDetailResponse> adminGetAllProducts(int page, int size, String sortBy, String direction);
    ProductDetailResponse       createProduct(CreateProductRequest request);
    ProductDetailResponse       updateProduct(String id, UpdateProductRequest request);
    ProductDetailResponse       patchProduct(String id, UpdateProductRequest request);
    void                        deleteProduct(String id);
    ProductDetailResponse       updateProductStatus(String id, UpdateProductStatusRequest request);
    ProductDetailResponse       updateInventory(String id, UpdateInventoryRequest request);
    Page<ProductResponse>       getLowStockProducts(int page, int size);
    ProductStatsResponse        getProductStatistics();
    List<ProductImageResponse>  uploadImages(String id, List<MultipartFile> files);
    void                        deleteImage(String productId, String imageId);

    BulkCreateResult bulkCreateProducts(BulkCreateProductRequest request);
}