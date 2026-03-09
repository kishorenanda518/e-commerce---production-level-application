package com.ecommerce.user_service.service;


import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.entity.UserProfile;
import com.ecommerce.user_service.enums.UserStatus;
import com.ecommerce.user_service.exception.OtpException;
import com.ecommerce.user_service.exception.ResendLimitExceededException;
import com.ecommerce.user_service.exception.ResourceNotFoundException;
import com.ecommerce.user_service.exception.UserAlreadyExistsException;
import com.ecommerce.user_service.mapper.UserMapper;
import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.repository.RoleRepository;
import com.ecommerce.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;

    private static final int MAX_RESEND_PER_HOUR = 3;

    @Override
    @Transactional   // whole registration is one atomic DB transaction
    public UserResponse registerUser(CreateUserRequest request) {

        log.info("Registering new user with email: {}", request.getEmail());

        // ── Step 1: Check for duplicate email ────────────────────────
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                "Email '" + request.getEmail() + "' is already registered"
            );
        }

        // ── Step 2: Check for duplicate username ─────────────────────
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                "Username '" + request.getUsername() + "' is already taken"
            );
        }

        // ── Step 3: Hash the password ─────────────────────────────────
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // ── Step 4: Load the default role ─────────────────────────────
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));

        // ── Step 5: Build and save the User entity ────────────────────
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .phone(request.getPhone())
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .roles(Set.of(userRole))
                .build();

        // ── Step 6: Build and attach UserProfile ──────────────────────
        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        user.setProfile(profile);

        // ── Step 7: Persist (cascade saves profile too) ───────────────
        User savedUser = userRepository.save(user);

        log.info("User registered successfully with id: {}", savedUser.getId());

        // ── Step 8: Map to response DTO and return ────────────────────
        return userMapper.toUserResponse(savedUser);
    }

    // ── VERIFY EMAIL ─────────────────────────────────────────────────
    @Override
    @Transactional
    public void verifyEmail(String email, String token) {

        log.info("Email verification attempt for: {}", email);

        // Step 1: Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Step 2: Check already verified
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new OtpException("Email is already verified");
        }

        // Step 3: Validate OTP from Redis
        boolean isValid = otpService.validateOtp(email, token);
        if (!isValid) {
            throw new OtpException("OTP is invalid or has expired. Please request a new one.");
        }

        // Step 4: Update user — verified + active
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Step 5: Delete OTP from Redis (prevent reuse)
        otpService.deleteOtp(email);

        log.info("Email verified successfully for: {}", email);
    }

    // ── RESEND VERIFICATION ──────────────────────────────────────────
    @Override
    public void resendVerificationEmail(String email) {

        log.info("Resend verification request for: {}", email);

        // Step 1: Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Step 2: Check already verified
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new OtpException("Email is already verified. No need to resend.");
        }

        // Step 3: Check resend limit (max 3 per hour)
        int currentCount = otpService.getResendCount(email);
        if (currentCount >= MAX_RESEND_PER_HOUR) {
            throw new ResendLimitExceededException(
                    "Maximum resend limit reached (" + MAX_RESEND_PER_HOUR + " per hour). " +
                            "Please try again after 1 hour."
            );
        }

        // Step 4: Increment resend counter
        otpService.incrementResendCount(email);

        // Step 5: Generate new OTP and send email
        String otp = otpService.generateAndStoreOtp(email);
        emailService.sendResendVerificationEmail(
                email,
                user.getProfile().getFirstName(),
                otp
        );

        log.info("Verification email resent to: {} | Attempt: {}", email, currentCount + 1);
    }
}