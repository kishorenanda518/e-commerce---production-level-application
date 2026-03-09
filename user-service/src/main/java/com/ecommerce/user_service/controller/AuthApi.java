package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.request.ResendVerificationRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name        = "Authentication",
        description = "APIs for user registration, login, email verification, and password management"
)
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @PostMapping("/register")
    @Operation(
            summary     = "Register a new user",
            description = "Creates a new user account. Username and email must be unique. " +
                    "Password must contain uppercase, lowercase, digit and special character."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description  = "User registered successfully",
                    content      = @Content(
                            mediaType = "application/json",
                            examples  = @ExampleObject(value = """
                    {
                      "status": "SUCCESS",
                      "message": "Registration successful. Welcome John!",
                      "timestamp": "2024-03-07T10:30:00Z",
                      "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "username": "john_doe",
                        "email": "john@example.com",
                        "status": "PENDING_VERIFICATION",
                        "emailVerified": false,
                        "roles": ["ROLE_USER"],
                        "firstName": "John",
                        "lastName": "Doe",
                        "createdAt": "2024-03-07T10:30:00Z"
                      }
                    }
                """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description  = "Validation failed — check fieldErrors for details",
                    content      = @Content(
                            mediaType = "application/json",
                            examples  = @ExampleObject(value = """
                    {
                      "status": 400,
                      "error": "VALIDATION_FAILED",
                      "message": "Input validation failed for 2 field(s)",
                      "path": "/api/v1/auth/register",
                      "timestamp": "2024-03-07T10:30:00Z",
                      "fieldErrors": [
                        { "field": "email", "rejectedValue": "notanemail", "message": "must be a valid email address" },
                        { "field": "password", "rejectedValue": "123", "message": "size must be between 8 and 100" }
                      ]
                    }
                """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description  = "Email or username already exists",
                    content      = @Content(
                            mediaType = "application/json",
                            examples  = @ExampleObject(value = """
                    {
                      "status": 409,
                      "error": "USER_ALREADY_EXISTS",
                      "message": "Email 'john@example.com' is already registered",
                      "path": "/api/v1/auth/register",
                      "timestamp": "2024-03-07T10:30:00Z"
                    }
                """)
                    )
            )
    })
    ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest request
    );

    // ── 2. VERIFY EMAIL ───────────────────────────────────────────────
    @GetMapping("/verify-email")
    @Operation(
            summary     = "Verify email using OTP",
            description = "Validates the OTP sent to the user's email. Sets emailVerified=true and status=ACTIVE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Email verified successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                      "status": "SUCCESS",
                      "message": "Email verified successfully. You can now login."
                    }
                """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "OTP invalid or expired",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                      "status": 400,
                      "error": "OTP_INVALID",
                      "message": "OTP is invalid or has expired. Please request a new one."
                    }
                """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "User not found"
            )
    })
    ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "OTP sent to email", required = true, example = "482910")
            @RequestParam String token,
            @Parameter(description = "Email address to verify", required = true, example = "john@example.com")
            @RequestParam String email
    );

    // ── 3. RESEND VERIFICATION ────────────────────────────────────────
    @PostMapping("/resend-verification")
    @Operation(
            summary     = "Resend OTP verification email",
            description = "Resends the OTP to the user's email. Maximum 3 resends per hour per email."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "OTP resent successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                      "status": "SUCCESS",
                      "message": "Verification email resent successfully. Please check your inbox."
                    }
                """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429", description = "Resend limit exceeded",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                      "status": 429,
                      "error": "RESEND_LIMIT_EXCEEDED",
                      "message": "Maximum resend limit reached (3 per hour). Please try again after 1 hour."
                    }
                """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "User not found"
            )
    })
    ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    );
}