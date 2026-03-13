package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.enums.UserStatus;
import com.ecommerce.user_service.exception.*;
import com.ecommerce.user_service.kafka.KafkaEventPublisher;
import com.ecommerce.user_service.kafka.KafkaTopics;
import com.ecommerce.user_service.kafka.event.UserPasswordChangedEvent;
import com.ecommerce.user_service.kafka.event.UserRegisteredEvent;
import com.ecommerce.user_service.model.request.ForgotPasswordRequest;
import com.ecommerce.user_service.model.request.ResetPasswordRequest;
import com.ecommerce.user_service.model.response.ApiResponse;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.service.EmailService;
import com.ecommerce.user_service.service.OtpService;
import com.ecommerce.user_service.util.CommonMethods;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@RestController
public class UserControllerImpl implements UserController {

    private final UserRepository        userRepository;
    private final OtpService            otpService;
    private final EmailService          emailService;
    private final CommonMethods         commonMethods;
    private final BCryptPasswordEncoder passwordEncoder;
    private final KafkaEventPublisher kafkaEventPublisher;


    private static final int MAX_RESEND_PER_HOUR = 3;

    // ── FORGOT PASSWORD ──────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> forgotPassword(ForgotPasswordRequest request) {

        log.info("Forgot password request for: {}", request.getEmail());

        // Step 1: Find user — return 200 even if not found (security)
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user != null) {

            // Step 2: Check suspended
            if (user.getStatus() == UserStatus.SUSPENDED) {
                throw new AccountSuspendedException(
                        "Your account has been suspended. Please contact support."
                );
            }

            // Step 3: Check rate limit (max 3 per hour)
            int currentCount = commonMethods.getForgotPasswordCount(request.getEmail());
            if (currentCount >= MAX_RESEND_PER_HOUR) {
                throw new ResendLimitExceededException(
                        "Maximum reset attempts reached (" + MAX_RESEND_PER_HOUR + " per hour). " +
                                "Please try again after 1 hour."
                );
            }

            // Step 4: Generate OTP and store in Redis (TTL 10 min)
            String otp = otpService.generateAndStoreOtp(request.getEmail());

            // Step 5: Store in forgot password Redis key
            commonMethods.storeForgotPasswordOtp(request.getEmail(), otp);

            // Step 6: Increment rate limit counter
            commonMethods.incrementForgotPasswordCount(request.getEmail());

            // Step 7: Send email (async)
            emailService.sendPasswordResetEmail(
                    request.getEmail(),
                    user.getProfile().getFirstName(),
                    otp
            );

            log.info("Password reset OTP sent to: {}", request.getEmail());
        }

        // Always return 200 — don't reveal if email exists
        return ResponseEntity.ok(
                ApiResponse.success(
                        "If this email is registered, you will receive a password reset OTP shortly.",
                        null
                )
        );
    }

    // ── RESET PASSWORD ───────────────────────────────────────────────
    @Override
    public ResponseEntity<ApiResponse<Void>> resetPassword(ResetPasswordRequest request) {

        log.info("Reset password request for: {}", request.getEmail());

        // Step 1: Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Step 2: Get OTP from Redis
        String storedOtp = commonMethods.getForgotPasswordOtp(request.getEmail());

        if (storedOtp == null) {
            throw new OtpException("OTP has expired. Please request a new one.");
        }

        if (!storedOtp.equals(request.getOtp())) {
            throw new OtpException("Invalid OTP. Please check and try again.");
        }

        // Step 3: Check new password is not same as old
//        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
//            throw new SamePasswordException(
//                    "New password cannot be the same as your current password."
//            );
//        }

        // Step 4: Hash and save new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password updated in DB for: {}", request.getEmail());

        // Step 5: Delete OTP from Redis (prevent reuse)
        commonMethods.deleteForgotPasswordOtp(request.getEmail());

        // Step 6: Delete refresh token → force re-login on all devices
        commonMethods.deleteRefreshToken(user.getId());

        // Step 7: Clear rate limit counter
        commonMethods.deleteForgotPasswordCount(request.getEmail());

        log.info("Password reset successfully for: {}", request.getEmail());

        kafkaEventPublisher.publish(
                KafkaTopics.USER_PASSWORD_CHANGED,
                user.getId(),
                UserPasswordChangedEvent.builder()
                        .userId(user.getId())
                        .ipAddress("543541sgsedr")
                        .timestamp(Instant.now())
                        .build());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully. Please login with your new password.",
                        null
                )
        );
    }
}