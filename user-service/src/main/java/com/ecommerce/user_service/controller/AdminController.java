package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.model.request.AssignRoleRequest;
import com.ecommerce.user_service.model.request.ChangeStatusRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.AuditLogResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Admin user management APIs")
@RequestMapping("/api/v1/admin")
public interface AdminController {

    @Operation(summary = "List all users paginated")
    @GetMapping("/users")
    ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    );

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{userId}")
    ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable String userId
    );

    @Operation(summary = "Change user status")
    @PatchMapping("/users/{userId}/status")
    ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody ChangeStatusRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Assign role to user")
    @PatchMapping("/users/{userId}/role")
    ResponseEntity<ApiResponse<UserResponse>> assignRole(
            @PathVariable String userId,
            @Valid @RequestBody AssignRoleRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(summary = "Get audit logs for a user")
    @GetMapping("/users/{userId}/audit-logs")
    ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @PathVariable String userId
    );
}