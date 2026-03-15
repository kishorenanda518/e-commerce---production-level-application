package com.ecommerce.user_service.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 15, message = "Phone must not exceed 15 characters")
    private String phone;
}