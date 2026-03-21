package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.*;
import com.ecommerce.product_service.enums.MovementType;
import com.ecommerce.product_service.enums.ProductStatus;
import com.ecommerce.product_service.exception.*;
import com.ecommerce.product_service.kafka.KafkaEventPublisher;
import com.ecommerce.product_service.kafka.KafkaTopics;
import com.ecommerce.product_service.kafka.event.*;
import com.ecommerce.product_service.model.request.*;
import com.ecommerce.product_service.model.response.*;
import com.ecommerce.product_service.repository.*;
import com.ecommerce.product_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository          productRepository;
    private final CategoryRepository         categoryRepository;
    private final ProductInventoryRepository inventoryRepository;
    private final ProductReviewRepository    reviewRepository;
    private final StockMovementRepository    stockMovementRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper               objectMapper;

    private static final String UPLOAD_DIR      = "uploads/product-images/";
    private static final int    MAX_IMAGES      = 10;

    // ════════════════════════════════════════════════════════════════
    // PUBLIC APIs
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(ProductFilterRequest filter) {
        Pageable pageable = buildPageable(
                filter.getPage(), filter.getSize(), filter.getSort());
        String categoryId = nullIfBlank(filter.getCategoryId());

        if (categoryId != null) {
            // check if this category has children
            List<String> childIds = categoryRepository
                    .findByParentIdAndIsActiveTrue(categoryId)
                    .stream()
                    .map(cat -> cat.getId())
                    .collect(Collectors.toList());

            if (!childIds.isEmpty()) {
                // parent category clicked → fetch from ALL child categories
                log.info("Parent category {} has {} children — fetching all",
                        categoryId, childIds.size());
                return productRepository.findByStatusAndCategoryIdIn(
                        ProductStatus.ACTIVE,
                        childIds,
                        pageable
                ).map(this::toProductResponse);
            }
        }
        // use dynamic filter query — null params are ignored in WHERE
        return productRepository.findWithFilters(
                ProductStatus.ACTIVE,
                nullIfBlank(filter.getCategoryId()),
                nullIfBlank(filter.getBrand()),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                nullIfBlank(filter.getQ()),
                filter.getInStock(),
                pageable
        ).map(this::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(ProductFilterRequest filter) {
        // searchProducts just delegates to getAllProducts — same logic
        return getAllProducts(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(String id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        return toProductDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySku(String sku) {
        Product product = productRepository.findBySkuAndStatusNot(sku, ProductStatus.DELETED)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        return toProductDetailResponse(product);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getRelatedProducts(String id, int page, int size) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
        Pageable pageable = PageRequest.of(page, size);
        return productRepository
                .findByCategoryIdAndStatus(product.getCategory().getId(), ProductStatus.ACTIVE, pageable)
                .map(this::toProductResponse);
    }

    @Override
    public Page<ReviewResponse> getProductReviews(String id, int page, int size, String sort) {
        log.debug("Fetching reviews for product: {}", id);
        Pageable pageable = PageRequest.of(page, size,
                sort.equals("rating") ? Sort.by("rating").descending() : Sort.by("createdAt").descending());
        return reviewRepository.findByProductIdAndIsApprovedTrue(id, pageable)
                .map(this::toReviewResponse);
    }

    @Override
    @Transactional
    public ReviewResponse submitReview(String productId, String userId, CreateReviewRequest request) {
        log.info("Submitting review for product: {} by user: {}", productId, userId);

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new ReviewAlreadyExistsException("You have already reviewed this product.");
        }

        Product product = findActiveProductById(productId);

        ProductReview review = ProductReview.builder()
                .product(product)
                .userId(userId)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .isApproved(true)
                .build();

        ProductReview saved = reviewRepository.save(review);

        // Update product average rating
        updateProductRating(product);

        log.info("Review submitted for product: {}", productId);
        return toReviewResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("soldCount").descending());
        return productRepository.findByStatusWithDetails(ProductStatus.ACTIVE, pageable)
                .map(this::toProductResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getNewArrivals(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByStatusWithDetails(ProductStatus.ACTIVE, pageable)
                .map(this::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getBestSellers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("soldCount").descending());
        return productRepository.findByStatusWithDetails(ProductStatus.ACTIVE, pageable)
                .map(this::toProductResponse);
    }

    // ════════════════════════════════════════════════════════════════
    // ADMIN APIs
    // ════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDetailResponse> adminGetAllProducts(
            int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable)
                .map(this::toProductDetailResponse);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        // Step 1: Check SKU uniqueness
        if (productRepository.existsBySku(request.getSku())) {
            throw new ProductAlreadyExistsException(
                    "Product with SKU '" + request.getSku() + "' already exists.");
        }

        // Step 2: Validate price
        validatePrice(request.getPrice(), request.getCompareAtPrice());

        // Step 3: Validate category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found: " + request.getCategoryId()));

        // Step 4: Build product
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .sku(request.getSku())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .category(category)
                .brandName(request.getBrandName())
                .weight(request.getWeight())
                .tags(toJson(request.getTags()))
                .attributes(toJson(request.getAttributes()))
                .status(ProductStatus.DRAFT)
                .build();

        // Step 5: Save images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = buildImages(request.getImageUrls(), product);
            product.setImages(images);
        }

        Product saved = productRepository.save(product);

        // Step 6: Create inventory
        ProductInventory inventory = ProductInventory.builder()
                .product(saved)
                .quantity(request.getInitialStock())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();
        inventoryRepository.save(inventory);

        // Step 7: Save stock movement
        saveStockMovement(saved, MovementType.PURCHASE, request.getInitialStock(),
                null, "Initial stock on product creation");

        // Step 8: Publish Kafka event
        kafkaEventPublisher.publish(
                KafkaTopics.PRODUCT_CREATED,
                saved.getId(),
                ProductCreatedEvent.builder()
                        .productId(saved.getId())
                        .name(saved.getName())
                        .sku(saved.getSku())
                        .categoryId(category.getId())
                        .price(saved.getPrice())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Product created successfully: {} | id: {}", saved.getSku(), saved.getId());
        return toProductDetailResponse(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(String id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = findProductById(id);
        BigDecimal oldPrice = product.getPrice();

        applyProductUpdates(product, request);
        Product updated = productRepository.save(product);

        // Publish price changed event if price changed
        if (request.getPrice() != null && request.getPrice().compareTo(oldPrice) != 0) {
            kafkaEventPublisher.publish(
                    KafkaTopics.PRICE_CHANGED,
                    id,
                    PriceChangedEvent.builder()
                            .productId(id)
                            .oldPrice(oldPrice)
                            .newPrice(request.getPrice())
                            .timestamp(Instant.now())
                            .build()
            );
        }

        kafkaEventPublisher.publish(
                KafkaTopics.PRODUCT_UPDATED,
                id,
                ProductUpdatedEvent.builder()
                        .productId(id)
                        .updatedFields(getNonNullFields(request))
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Product updated: {}", id);
        return toProductDetailResponse(updated);
    }

    @Override
    @Transactional
    public ProductDetailResponse patchProduct(String id, UpdateProductRequest request) {
        // Same as update — partial fields handled by null checks in applyProductUpdates
        return updateProduct(id, request);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        log.info("Soft deleting product: {}", id);

        Product product = findProductById(id);
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);

        kafkaEventPublisher.publish(
                KafkaTopics.PRODUCT_DELETED,
                id,
                ProductDeletedEvent.builder()
                        .productId(id)
                        .sku(product.getSku())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Product soft deleted: {}", id);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProductStatus(String id, UpdateProductStatusRequest request) {
        log.info("Updating product status: {} → {}", id, request.getStatus());

        Product product = findProductById(id);
        product.setStatus(request.getStatus());
        Product updated = productRepository.save(product);

        log.info("Product status updated: {} → {}", id, request.getStatus());
        return toProductDetailResponse(updated);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateInventory(String id, UpdateInventoryRequest request) {
        log.info("Updating inventory for product: {} | op: {} | qty: {}",
                id, request.getOperation(), request.getQuantity());

        Product product = findProductById(id);
        ProductInventory inventory = inventoryRepository.findByProductId(id)
                .orElseThrow(() -> new ProductNotFoundException("Inventory not found for product: " + id));

        int oldQty = inventory.getQuantity();
        int newQty = switch (request.getOperation().toUpperCase()) {
            case "ADD"      -> oldQty + request.getQuantity();
            case "SUBTRACT" -> {
                if (oldQty < request.getQuantity()) {
                    throw new InsufficientStockException(
                            "Cannot subtract " + request.getQuantity() + " from stock of " + oldQty);
                }
                yield oldQty - request.getQuantity();
            }
            case "SET" -> request.getQuantity();
            default -> throw new IllegalArgumentException(
                    "Invalid operation: " + request.getOperation() + ". Use ADD, SUBTRACT or SET.");
        };

        inventory.setQuantity(newQty);
        inventoryRepository.save(inventory);

        // Save stock movement
        MovementType movementType = request.getOperation().equalsIgnoreCase("SUBTRACT")
                ? MovementType.SALE : MovementType.ADJUSTMENT;
        saveStockMovement(product, movementType, request.getQuantity(),
                null, request.getReason());

        // Publish stock updated event
        kafkaEventPublisher.publish(
                KafkaTopics.STOCK_UPDATED,
                id,
                StockUpdatedEvent.builder()
                        .productId(id)
                        .oldQty(oldQty)
                        .newQty(newQty)
                        .operation(request.getOperation())
                        .timestamp(Instant.now())
                        .build()
        );

        // Check low stock / out of stock
        if (newQty == 0) {
            kafkaEventPublisher.publish(KafkaTopics.OUT_OF_STOCK, id,
                    OutOfStockEvent.builder()
                            .productId(id).name(product.getName())
                            .timestamp(Instant.now()).build());
        } else if (newQty <= inventory.getLowStockThreshold()) {
            kafkaEventPublisher.publish(KafkaTopics.LOW_STOCK_ALERT, id,
                    LowStockAlertEvent.builder()
                            .productId(id).name(product.getName())
                            .currentQty(newQty).threshold(inventory.getLowStockThreshold())
                            .timestamp(Instant.now()).build());
        } else if (oldQty == 0 && newQty > 0) {
            kafkaEventPublisher.publish(KafkaTopics.BACK_IN_STOCK, id,
                    BackInStockEvent.builder()
                            .productId(id).name(product.getName())
                            .newQty(newQty).timestamp(Instant.now()).build());
        }

        log.info("Inventory updated for product: {} | {} → {}", id, oldQty, newQty);
        return toProductDetailResponse(productRepository.findById(id).orElseThrow());
    }

    @Override
    public Page<ProductResponse> getLowStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findLowStockProducts(pageable)
                .map(this::toProductResponse);
    }

    @Override
    public ProductStatsResponse getProductStatistics() {
        long total    = productRepository.count();
        long active   = productRepository.findByStatus(ProductStatus.ACTIVE,   PageRequest.of(0, 1)).getTotalElements();
        long draft    = productRepository.findByStatus(ProductStatus.DRAFT,    PageRequest.of(0, 1)).getTotalElements();
        long inactive = productRepository.findByStatus(ProductStatus.INACTIVE, PageRequest.of(0, 1)).getTotalElements();
        long outOfStock = inventoryRepository.findAll().stream()
                .filter(i -> i.getQuantity() == 0).count();
        long lowStock = inventoryRepository.findAll().stream()
                .filter(i -> i.getQuantity() > 0 && i.getQuantity() <= i.getLowStockThreshold()).count();

        return ProductStatsResponse.builder()
                .total(total).active(active).draft(draft)
                .inactive(inactive).outOfStock(outOfStock).lowStock(lowStock)
                .build();
    }

    @Override
    @Transactional
    public List<ProductImageResponse> uploadImages(String productId, List<MultipartFile> files) {
        log.info("Uploading {} images for product: {}", files.size(), productId);

        Product product = findProductById(productId);

        if (product.getImages().size() + files.size() > MAX_IMAGES) {
            throw new ImageLimitException(
                    "Cannot exceed " + MAX_IMAGES + " images per product.");
        }

        List<ProductImage> saved = new ArrayList<>();

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileName = productId + "_" + UUID.randomUUID() + getExtension(file.getOriginalFilename());
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(UPLOAD_DIR + fileName)
                        .displayOrder(product.getImages().size() + i)
                        .isPrimary(product.getImages().isEmpty() && i == 0)
                        .build();

                product.getImages().add(image);
            }

            productRepository.save(product);
            log.info("Images uploaded for product: {}", productId);

        } catch (IOException e) {
            log.error("Failed to upload images for product: {}", productId, e);
            throw new RuntimeException("Failed to upload images.");
        }

        return product.getImages().stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteImage(String productId, String imageId) {
        log.info("Deleting image: {} from product: {}", imageId, productId);

        Product product = findProductById(productId);
        product.getImages().removeIf(img -> img.getId().equals(imageId));
        productRepository.save(product);

        log.info("Image deleted: {}", imageId);
    }

    @Override
    @Transactional
    public BulkCreateResult bulkCreateProducts(BulkCreateProductRequest request) {
        log.info("Bulk creating {} products", request.getProducts().size());

        List<ProductDetailResponse>         created = new ArrayList<>();
        List<BulkCreateResult.FailedProduct> failed = new ArrayList<>();

        for (CreateProductRequest productRequest : request.getProducts()) {
            try {
                ProductDetailResponse product = createProduct(productRequest);
                created.add(product);
                log.info("Bulk create success: {}", productRequest.getSku());
            } catch (Exception e) {
                log.warn("Bulk create failed for SKU: {} | reason: {}",
                        productRequest.getSku(), e.getMessage());
                failed.add(BulkCreateResult.FailedProduct.builder()
                        .sku(productRequest.getSku())
                        .name(productRequest.getName())
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("Bulk create complete — success: {} | failed: {}",
                created.size(), failed.size());

        return BulkCreateResult.builder()
                .totalRequested(request.getProducts().size())
                .totalSuccess(created.size())
                .totalFailed(failed.size())
                .createdProducts(created)
                .failedProducts(failed)
                .build();
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════

    private Product findActiveProductById(String id) {
        return productRepository.findById(id)
                .filter(p -> p.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    private void validatePrice(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) < 0) {
            throw new InvalidPriceException("Compare-at price must be greater than or equal to selling price.");
        }
    }

    private void applyProductUpdates(Product product, UpdateProductRequest request) {
        if (request.getName()             != null) product.setName(request.getName());
        if (request.getDescription()      != null) product.setDescription(request.getDescription());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getPrice()            != null) product.setPrice(request.getPrice());
        if (request.getCompareAtPrice()   != null) product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getCostPrice()        != null) product.setCostPrice(request.getCostPrice());
        if (request.getBrandName()        != null) product.setBrandName(request.getBrandName());
        if (request.getWeight()           != null) product.setWeight(request.getWeight());
        if (request.getTags()             != null) product.setTags(toJson(request.getTags()));
        if (request.getAttributes()       != null) product.setAttributes(toJson(request.getAttributes()));
        if (request.getCategoryId()       != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
    }

    private void saveStockMovement(Product product, MovementType type,
                                    int quantity, String referenceId, String reason) {
        StockMovement movement = StockMovement.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .referenceId(referenceId)
                .reason(reason)
                .build();
        stockMovementRepository.save(movement);
    }

    private void updateProductRating(Product product) {
        List<ProductReview> reviews = reviewRepository
                .findByProductIdAndIsApprovedTrue(product.getId(), Pageable.unpaged())
                .toList();
        double avg = reviews.stream()
                .mapToInt(ProductReview::getRating)
                .average().orElse(0.0);
        product.setAverageRating(avg);
        product.setReviewCount(reviews.size());
        productRepository.save(product);
    }

    private List<ProductImage> buildImages(List<String> urls, Product product) {
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            images.add(ProductImage.builder()
                    .product(product)
                    .imageUrl(urls.get(i))
                    .displayOrder(i)
                    .isPrimary(i == 0)
                    .build());
        }
        return images;
    }

    private Pageable buildPageable(int page, int size, String sort) {
        return switch (sort) {
            case "price,asc"   -> PageRequest.of(page, size, Sort.by("price").ascending());
            case "price,desc"  -> PageRequest.of(page, size, Sort.by("price").descending());
            case "rating,desc" -> PageRequest.of(page, size, Sort.by("averageRating").descending());
            case "best-selling"-> PageRequest.of(page, size, Sort.by("soldCount").descending());
            default            -> PageRequest.of(page, size, Sort.by("createdAt").descending());
        };
    }

    private List<String> getNonNullFields(UpdateProductRequest request) {
        List<String> fields = new ArrayList<>();
        if (request.getName()             != null) fields.add("name");
        if (request.getDescription()      != null) fields.add("description");
        if (request.getPrice()            != null) fields.add("price");
        if (request.getCategoryId()       != null) fields.add("categoryId");
        if (request.getBrandName()        != null) fields.add("brandName");
        if (request.getTags()             != null) fields.add("tags");
        if (request.getAttributes()       != null) fields.add("attributes");
        return fields;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }

    // ── MAPPERS ───────────────────────────────────────────────────────

    private ProductResponse toProductResponse(Product product) {
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl).collect(Collectors.toList());

        boolean inStock = product.getInventory() != null
                && product.getInventory().getQuantity() > 0;

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
                .inStock(inStock)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductDetailResponse toProductDetailResponse(Product product) {
        InventoryResponse inventoryResponse = null;
        if (product.getInventory() != null) {
            ProductInventory inv = product.getInventory();
            int available = inv.getQuantity() - inv.getReservedQuantity();
            inventoryResponse = InventoryResponse.builder()
                    .id(inv.getId())
                    .quantity(inv.getQuantity())
                    .reservedQuantity(inv.getReservedQuantity())
                    .availableQuantity(available)
                    .lowStockThreshold(inv.getLowStockThreshold())
                    .isLowStock(inv.getQuantity() <= inv.getLowStockThreshold())
                    .isOutOfStock(inv.getQuantity() == 0)
                    .warehouseLocation(inv.getWarehouseLocation())
                    .lastUpdated(inv.getLastUpdated())
                    .build();
        }

        List<ProductImageResponse> images = product.getImages().stream()
                .map(this::toImageResponse).collect(Collectors.toList());

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .costPrice(product.getCostPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrandName())
                .status(product.getStatus())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .viewCount(product.getViewCount())
                .soldCount(product.getSoldCount())
                .weight(product.getWeight())
                .images(images)
                .inventory(inventoryResponse)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductImageResponse toImageResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .build();
    }

    private ReviewResponse toReviewResponse(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .build();
    }
    // ── helper ────────────────────────────────────────────────────
    private String nullIfBlank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}