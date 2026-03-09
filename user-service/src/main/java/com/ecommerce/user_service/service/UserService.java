package com.ecommerce.user_service.service;


import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.response.UserResponse;

public interface UserService {

    /**
     * Register a new user.
     * - Checks duplicate email / username
     * - Hashes password with BCrypt
     * - Assigns ROLE_USER
     * - Creates linked UserProfile
     * - Returns UserResponse
     */
    UserResponse registerUser(CreateUserRequest request);
    void verifyEmail(String email, String token);
    void resendVerificationEmail(String email);
}