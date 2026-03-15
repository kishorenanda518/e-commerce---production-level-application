package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.model.request.*;
import com.ecommerce.user_service.model.response.AddressResponse;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "User", description = "User profile management APIs")
@RequestMapping("/api/v1/users")
public interface UserController {

    @Operation(summary = "Forgot password — send OTP to email")
    @PostMapping("/forgot-password")
    ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    );

    @Operation(summary = "Reset password using OTP")
    @PostMapping("/reset-password")
    ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            HttpServletRequest request
    );

    @Operation(summary = "Update my profile")
    @PutMapping("/me")
    ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Change my password")
    @PatchMapping("/me/password")
    ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Delete my account")
    @DeleteMapping("/me")
    ResponseEntity<ApiResponse<Void>> deleteMyAccount(
            HttpServletRequest request
    );

    @Operation(summary = "Upload profile picture")
    @PostMapping(value = "/me/profile-picture", consumes = "multipart/form-data")
    ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    );


    // ── ADDRESS APIs ──────────────────────────────────────────────────

    @Operation(summary = "Get all my addresses")
    @GetMapping("/me/addresses")
    ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            HttpServletRequest request
    );

    @Operation(summary = "Add a new address")
    @PostMapping("/me/addresses")
    ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Update an address")
    @PutMapping("/me/addresses/{addressId}")
    ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable String addressId,    // ← String
            @Valid @RequestBody AddressRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Delete an address")
    @DeleteMapping("/me/addresses/{addressId}")
    ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable String addressId,    // ← String
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Set an address as default")
    @PatchMapping("/me/addresses/{addressId}/default")
    ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @PathVariable String addressId,    // ← String
            HttpServletRequest httpRequest
    );
}