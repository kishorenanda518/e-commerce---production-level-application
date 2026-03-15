package com.ecommerce.user_service.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    // ── User info ─────────────────────────────────────────────────────
    private String      id;
    private String      username;
    private String      email;
    private String      firstName;
    private String      lastName;
    private Set<String> roles;
    private String      tokenType;
    private long        expiresIn;
    private Instant     timestamp;

    // ── Products loaded on login ──────────────────────────────────────
    private ProductSummary products;

    @Data
    @Builder
    public static class ProductSummary {
        private long              totalProducts;
        private int               totalPages;
        private int               currentPage;
        private List<ProductItem> items;
    }

    @Data
    @Builder
    public static class ProductItem {
        private String       id;
        private String       name;
        private String       sku;
        private Double       price;
        private String       categoryName;
        private String       brandName;
        private Boolean      inStock;
        private List<String> imageUrls;
    }
}