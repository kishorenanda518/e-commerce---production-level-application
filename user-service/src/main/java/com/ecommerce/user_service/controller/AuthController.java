package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.request.LoginRequest;
import com.ecommerce.user_service.model.request.ResendVerificationRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.AuthResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;

    @Override
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid CreateUserRequest request) {

        log.info("Registration request received for email: {}", request.getEmail());

        UserResponse userResponse = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful. Welcome " + userResponse.getFirstName() + "!",
                        userResponse
                ));
    }

    // ── 2. VERIFY EMAIL ───────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            String token, String email) {

        log.info("Verify email request for: {}", email);

        userService.verifyEmail(email, token);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully. You can now login.", null)
        );
    }

    // ── 3. RESEND VERIFICATION ────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid ResendVerificationRequest request) {

        log.info("Resend verification request for: {}", request.getEmail());

        userService.resendVerificationEmail(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification email resent successfully. Please check your inbox.",
                        null
                )
        );
    }

    // ── 4. LOGIN ──────────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid LoginRequest request,
            HttpServletResponse response) {

        log.info("Login request for: {}", request.getUsernameOrEmail());

        AuthResponse authResponse = userService.login(request, response);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful. Welcome back " + authResponse.getFirstName() + "!",
                        authResponse
                )
        );
    }

    // ── 5. LOGOUT ─────────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("Logout request received");

        userService.logout(request, response);

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully.", null)
        );
    }

    @Override
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            int page, int size, String sortBy, String direction) {

        log.info("Fetching all users — page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, direction);

        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, direction);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }


    @Override
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request, HttpServletResponse response) {

        log.info("Refresh token request received");

        AuthResponse authResponse = userService.refreshToken(request, response);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully.", authResponse)
        );
    }
}