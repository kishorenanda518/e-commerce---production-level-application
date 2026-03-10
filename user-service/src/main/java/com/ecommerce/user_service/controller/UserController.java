package com.ecommerce.user_service.controller;


import com.ecommerce.user_service.model.request.ForgotPasswordRequest;
import com.ecommerce.user_service.model.request.ResetPasswordRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/v1/user")
public interface UserController {

    // ── FORGOT PASSWORD ───────────────────────────────────────────────
    @Operation(summary = "Forgot password — sends OTP to registered email")
    @PostMapping("/forgot-password")
    ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request);

    // ── RESET PASSWORD ────────────────────────────────────────────────
    @Operation(summary = "Reset password using OTP received via email")
    @PostMapping("/reset-password")
    ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request);

}
