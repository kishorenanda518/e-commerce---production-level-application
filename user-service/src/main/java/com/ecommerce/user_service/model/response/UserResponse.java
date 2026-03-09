package com.ecommerce.user_service.model.response;

import com.ecommerce.user_service.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;


@Data
@Builder
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private String phone;
    private UserStatus status;
    private Boolean emailVerified;
    private Set<String> roles;       // e.g. ["ROLE_USER"]
    private String firstName;
    private String lastName;
    private Instant createdAt;
}