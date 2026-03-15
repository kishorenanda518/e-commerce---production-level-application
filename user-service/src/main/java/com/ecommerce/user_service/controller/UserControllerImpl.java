package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.entity.Address;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.exception.OtpException;
import com.ecommerce.user_service.exception.ResourceNotFoundException;
import com.ecommerce.user_service.exception.SamePasswordException;
import com.ecommerce.user_service.kafka.KafkaEventPublisher;
import com.ecommerce.user_service.kafka.KafkaTopics;
import com.ecommerce.user_service.kafka.event.UserDeletedEvent;
import com.ecommerce.user_service.kafka.event.UserPasswordChangedEvent;
import com.ecommerce.user_service.kafka.event.UserUpdatedEvent;
import com.ecommerce.user_service.mapper.UserMapper;
import com.ecommerce.user_service.model.request.*;
import com.ecommerce.user_service.model.response.AddressResponse;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.repository.AddressRepository;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.security.JwtUtil;
import com.ecommerce.user_service.service.EmailService;
import com.ecommerce.user_service.service.OtpService;
import com.ecommerce.user_service.util.CommonMethods;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserRepository        userRepository;
    private final EmailService          emailService;
    private final OtpService            otpService;
    private final CommonMethods         commonMethods;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KafkaEventPublisher   kafkaEventPublisher;
    private final JwtUtil               jwtUtil;
    private final UserMapper            userMapper;
    private final AddressRepository addressRepository;

    private static final int    MAX_RESET_PER_HOUR    = 3;
    private static final long   MAX_FILE_SIZE_BYTES   = 5 * 1024 * 1024; // 5 MB
    private static final String UPLOAD_DIR            = "uploads/profile-pictures/";

    // ── FORGOT PASSWORD ───────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest request) {

        log.info("Forgot password request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        int count = commonMethods.getForgotPasswordCount(request.getEmail());
        if (count >= MAX_RESET_PER_HOUR) {
            throw new OtpException("Too many attempts. Please try again after 1 hour.");
        }

        String otp = otpService.generateAndStoreOtp(request.getEmail());
        commonMethods.storeForgotPasswordOtp(request.getEmail(), otp);
        commonMethods.incrementForgotPasswordCount(request.getEmail());

        emailService.sendPasswordResetEmail(request.getEmail(), user.getProfile().getFirstName(), otp);

        log.info("Password reset OTP sent to: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset OTP sent to your email.", null));
    }

    // ── RESET PASSWORD ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            ResetPasswordRequest request, HttpServletRequest httpRequest) {

        log.info("Reset password request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String storedOtp = commonMethods.getForgotPasswordOtp(request.getEmail());
        if (storedOtp == null) {
            throw new OtpException("OTP has expired. Please request a new one.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new OtpException("Invalid OTP. Please check and try again.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new SamePasswordException("New password cannot be the same as your current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        commonMethods.deleteForgotPasswordOtp(request.getEmail());
        commonMethods.deleteRefreshToken(user.getId());
        commonMethods.deleteForgotPasswordCount(request.getEmail());

        kafkaEventPublisher.publish(
                KafkaTopics.USER_PASSWORD_CHANGED,
                user.getId(),
                UserPasswordChangedEvent.builder()
                        .userId(user.getId())
                        .ipAddress(commonMethods.extractIpAddress(httpRequest))
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Password reset successfully for: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. Please login with your new password.", null));
    }

    // ── GET MY PROFILE ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(HttpServletRequest request) {

        String userId = extractUserIdFromRequest(request);
        log.info("Get profile request for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully.", userMapper.toUserResponse(user)));
    }

    // ── UPDATE MY PROFILE ─────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            UpdateProfileRequest request, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Update profile request for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) user.getProfile().setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.getProfile().setLastName(request.getLastName());
        if (request.getPhone()     != null) user.setPhone(request.getPhone());

        userRepository.save(user);

        kafkaEventPublisher.publish(
                KafkaTopics.USER_UPDATED,
                userId,
                UserUpdatedEvent.builder()
                        .userId(userId)
                        .updatedFields(List.of("firstName", "lastName", "phone"))
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Profile updated for userId: {}", userId);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", userMapper.toUserResponse(user)));
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> changePassword(
            ChangePasswordRequest request, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Change password request for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new SamePasswordException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new SamePasswordException("New password cannot be the same as your current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        commonMethods.deleteRefreshToken(userId);

        kafkaEventPublisher.publish(
                KafkaTopics.USER_PASSWORD_CHANGED,
                userId,
                UserPasswordChangedEvent.builder()
                        .userId(userId)
                        .ipAddress(commonMethods.extractIpAddress(httpRequest))
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Password changed successfully for userId: {}", userId);
        return ResponseEntity.ok(ApiResponse.success(
                "Password changed successfully. Please login again.", null));
    }

    // ── DELETE MY ACCOUNT ─────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount(HttpServletRequest request) {

        String userId = extractUserIdFromRequest(request);
        log.info("Delete account request for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String email = user.getEmail();
        userRepository.delete(user);
        commonMethods.deleteRefreshToken(userId);

        kafkaEventPublisher.publish(
                KafkaTopics.USER_DELETED,
                userId,
                UserDeletedEvent.builder()
                        .userId(userId)
                        .email(email)
                        .reason("User self-deletion")
                        .timestamp(Instant.now())
                        .build()
        );

        log.info("Account deleted for userId: {}", userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully.", null));
    }

    // ── UPLOAD PROFILE PICTURE ────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            MultipartFile file, HttpServletRequest request) {

        String userId = extractUserIdFromRequest(request);
        log.info("Profile picture upload for userId: {}", userId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must not exceed 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String extension = getExtension(file.getOriginalFilename());
            String fileName  = userId + "_" + UUID.randomUUID() + "." + extension;
            Path   filePath  = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.getProfile().setProfilePictureUrl(UPLOAD_DIR + fileName);
            userRepository.save(user);

            log.info("Profile picture uploaded for userId: {} → {}", userId, fileName);
            return ResponseEntity.ok(ApiResponse.success(
                    "Profile picture uploaded successfully.", userMapper.toUserResponse(user)));

        } catch (IOException e) {
            log.error("Failed to upload profile picture for userId: {}", userId, e);
            throw new RuntimeException("Failed to upload profile picture. Please try again.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            HttpServletRequest request) {

        String userId = extractUserIdFromRequest(request);
        log.info("Get addresses for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<AddressResponse> addresses = addressRepository.findByUser(user)
                .stream()
                .map(this::toAddressResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Addresses fetched successfully.", addresses));
    }

    // ── ADD ADDRESS ───────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            AddressRequest request, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Add address for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));


        // If new address is default → clear existing default first
        if (request.isDefaultAddress()) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .addressType(request.getAddressType())
                .isDefault(request.isDefaultAddress())
                .build();
        Address saved = addressRepository.save(address);

        log.info("Address added for userId: {} | addressId: {}", userId, saved.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully.", toAddressResponse(saved)));
    }

    // ── UPDATE ADDRESS ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            String addressId, AddressRequest request, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Update address {} for userId: {}", addressId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", String.valueOf(addressId)));

        // If setting this as default → clear existing default
        if (request.isDefaultAddress() && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        addressRepository.save(existing);
                    });
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(request.getAddressType());
        address.setIsDefault(request.isDefaultAddress());

        Address updated = addressRepository.save(address);

        log.info("Address updated: {} for userId: {}", addressId, userId);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully.", toAddressResponse(updated)));
    }

    // ── DELETE ADDRESS ────────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            String addressId, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Delete address {} for userId: {}", addressId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", String.valueOf(addressId)));

        addressRepository.delete(address);

        log.info("Address deleted: {} for userId: {}", addressId, userId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully.", null));
    }

    // ── SET DEFAULT ADDRESS ───────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            String addressId, HttpServletRequest httpRequest) {

        String userId = extractUserIdFromRequest(httpRequest);
        log.info("Set default address {} for userId: {}", addressId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Clear existing default
        addressRepository.findByUserAndIsDefaultTrue(user);

        // Set new default
        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", String.valueOf(addressId)));

        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    addressRepository.save(existing);
                });

        address.setIsDefault(true);

        log.info("Default address set to: {} for userId: {}", addressId, userId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated successfully.", toAddressResponse(address)));
    }

    // ── ADDRESS MAPPER HELPER ─────────────────────────────────────────
    private AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .addressType(address.getAddressType())
                .isDefault(address.getIsDefault())
                .build();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────
    private String extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        // fallback — extract from cookie
        return jwtUtil.extractUserId(
                commonMethods.extractAccessTokenFromCookie(request)
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}