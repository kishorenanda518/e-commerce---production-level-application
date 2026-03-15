package com.ecommerce.product_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewResponse {
    private String  id;
    private String  userId;
    private Integer rating;
    private String  title;
    private String  comment;
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount;
    private Instant createdAt;
}