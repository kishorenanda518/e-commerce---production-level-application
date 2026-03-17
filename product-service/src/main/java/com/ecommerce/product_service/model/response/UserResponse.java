package com.ecommerce.product_service.model.response;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}