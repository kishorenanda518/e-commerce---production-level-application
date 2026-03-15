package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.entity.AuditLog;
import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.exception.ResourceNotFoundException;
import com.ecommerce.user_service.kafka.KafkaEventPublisher;
import com.ecommerce.user_service.kafka.KafkaTopics;
import com.ecommerce.user_service.kafka.event.UserRoleEvent;
import com.ecommerce.user_service.kafka.event.UserStatusChangedEvent;
import com.ecommerce.user_service.mapper.UserMapper;
import com.ecommerce.user_service.model.request.AssignRoleRequest;
import com.ecommerce.user_service.model.request.ChangeStatusRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.AuditLogResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.repository.AuditLogRepository;
import com.ecommerce.user_service.repository.RoleRepository;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.security.JwtUtil;
import com.ecommerce.user_service.util.CommonMethods;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminControllerImpl implements AdminController {

    private final UserRepository      userRepository;
    private final RoleRepository      roleRepository;
    private final AuditLogRepository  auditLogRepository;
    private final UserMapper          userMapper;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final JwtUtil             jwtUtil;
    private final CommonMethods       commonMethods;

    // ── LIST ALL USERS ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            int page, int size, String sortBy, String direction) {

        log.info("Admin: list users — page: {}, size: {}", page, size);

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> users = userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);

        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully.", users));
    }

    // ── GET USER BY ID ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(String userId) {

        log.info("Admin: get user by id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return ResponseEntity.ok(ApiResponse.success("User fetched successfully.", userMapper.toUserResponse(user)));
    }

    // ── CHANGE USER STATUS ────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> changeUserStatus(
            String userId, ChangeStatusRequest request, HttpServletRequest httpRequest) {

        log.info("Admin: change status for userId: {} → {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String oldStatus = user.getStatus().name();
        user.setStatus(request.getStatus());
        userRepository.save(user);

        // Save audit log
        List<AuditLogResponse> logs = auditLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId)   // ← userId string directly
                .stream()
                .map(this::toAuditLogResponse)
                .toList();

        kafkaEventPublisher.publish(
                KafkaTopics.USER_STATUS_CHANGED,
                userId,
                UserStatusChangedEvent.builder()
                        .userId(userId)
                        .oldStatus(oldStatus)
                        .newStatus(request.getStatus().name())
                        .changedBy(extractAdminId(httpRequest))
                        .reason(request.getReason())
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Status updated for userId: {}", userId);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully.", userMapper.toUserResponse(user)));
    }

    // ── ASSIGN ROLE ───────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> assignRole(
            String userId, AssignRoleRequest request, HttpServletRequest httpRequest) {

        log.info("Admin: assign role {} to userId: {}", request.getRoleName(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", request.getRoleName()));

        // Add role if not already assigned
        Set<Role> roles = user.getRoles();
        if (roles.contains(role)) {
            return ResponseEntity.ok(ApiResponse.success(
                    "User already has role: " + request.getRoleName(), userMapper.toUserResponse(user)));
        }

        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        String adminId = extractAdminId(httpRequest);

        saveAuditLog(
                userId,
                "ROLE_ASSIGNED",
                "User",
                userId,
                commonMethods.extractIpAddress(httpRequest),
                "{\"role\":\"" + request.getRoleName() + "\"}"
        );

        kafkaEventPublisher.publish(
                KafkaTopics.USER_ROLE_ASSIGNED,
                userId,
                UserRoleEvent.builder()
                        .userId(userId)
                        .roleName(request.getRoleName())
                        .assignedBy(adminId)
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Role {} assigned to userId: {}", request.getRoleName(), userId);
        return ResponseEntity.ok(ApiResponse.success(
                "Role assigned successfully.", userMapper.toUserResponse(user)));
    }

    // ── GET AUDIT LOGS ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(String userId) {

        log.info("Admin: get audit logs for userId: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<AuditLogResponse> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toAuditLogResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully.", logs));
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────
    private String extractAdminId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.extractUserId(authHeader.substring(7));
        }
        return jwtUtil.extractUserId(commonMethods.extractAccessTokenFromCookie(request));
    }

    private void saveAuditLog(String userId, String action, String entityType,
                              String entityId, String ipAddress, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .newValue(newValue)
                .build();
        auditLogRepository.save(auditLog);
    }
    private AuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())        // ← getUserId() not getUser().getId()
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}