package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkCreateResult {

    private int                       totalRequested;
    private int                       totalSuccess;
    private int                       totalFailed;
    private List<ProductDetailResponse> createdProducts;
    private List<FailedProduct>         failedProducts;

    @Data
    @Builder
    public static class FailedProduct {
        private String sku;
        private String name;
        private String reason;
    }
}