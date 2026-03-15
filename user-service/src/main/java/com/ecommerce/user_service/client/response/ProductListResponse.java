package com.ecommerce.user_service.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductListResponse {

    private String   status;
    private String   message;
    private PageData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageData {
        private List<ProductItem> content      = new ArrayList<>();
        private int               totalPages;
        private long              totalElements;
        private int               size;
        private int               number;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductItem {
        private String       id;
        private String       name;
        private String       sku;
        private Double       price;
        private String       categoryName;
        private String       brandName;
        private String       status;
        private Boolean      inStock;
        private List<String> imageUrls;
    }

    public static ProductListResponse empty() {
        ProductListResponse response = new ProductListResponse();
        response.setStatus("UNAVAILABLE");
        response.setMessage("Product service unavailable");
        response.setData(new PageData());
        return response;
    }
}