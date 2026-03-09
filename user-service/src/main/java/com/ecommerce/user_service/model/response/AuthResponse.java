package com.ecommerce.user_service.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {

    private String  id;
    private String  username;
    private String  email;
    private String  firstName;
    private String  lastName;
    private Set<String> roles;

    // Token info
    private String  tokenType;        // "Bearer"
    private long    expiresIn;        // access token TTL in seconds

    // Note: actual tokens are in HttpOnly cookies — NOT in response body
    // This keeps tokens safe from XSS attacks
}