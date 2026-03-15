package com.ecommerce.user_service.model.request;

import com.ecommerce.user_service.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    private String reason;
}