package com.ecommerce.user_service.service;


import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.request.LoginRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.AuthResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    /**
     * Register a new user.
     * - Checks duplicate email / username
     * - Hashes password with BCrypt
     * - Assigns ROLE_USER
     * - Creates linked UserProfile
     * - Returns UserResponse
     */
    UserDetails loadUserByUsername(String usernameOrEmail);
    UserResponse registerUser(CreateUserRequest request);
    void verifyEmail(String email, String token);
    void resendVerificationEmail(String email);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction);
}