package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.controller.AuthApi;
import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.request.ResendVerificationRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}