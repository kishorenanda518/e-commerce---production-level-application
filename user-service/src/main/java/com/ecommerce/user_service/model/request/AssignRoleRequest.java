package com.ecommerce.user_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;
}