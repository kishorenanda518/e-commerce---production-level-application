package com.ecommerce.product_service.model.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductFilterRequest {

    private int     page        = 0;
    private int     size        = 12;
    private String  sort        = "newest";

    // ── filters — null means "not applied" ───────────────────
    private String     categoryId  = null;
    private String     brand       = null;
    private Boolean    inStock     = null;
    private BigDecimal minPrice    = null;
    private BigDecimal maxPrice    = null;
    private String     q           = null;   // search query
}