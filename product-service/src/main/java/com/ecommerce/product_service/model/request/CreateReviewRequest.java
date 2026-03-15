package com.ecommerce.product_service.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String comment;
}