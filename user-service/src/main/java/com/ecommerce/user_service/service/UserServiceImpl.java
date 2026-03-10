package com.ecommerce.user_service.service;


import com.ecommerce.user_service.config.JwtProperties;
import com.ecommerce.user_service.entity.Role;
import com.ecommerce.user_service.entity.User;
import com.ecommerce.user_service.entity.UserProfile;
import com.ecommerce.user_service.enums.UserStatus;
import com.ecommerce.user_service.exception.*;
import com.ecommerce.user_service.mapper.UserMapper;
import com.ecommerce.user_service.model.request.CreateUserRequest;
import com.ecommerce.user_service.model.request.LoginRequest;
import com.ecommerce.user_service.model.response.AuthResponse;
import com.ecommerce.user_service.model.response.UserResponse;
import com.ecommerce.user_service.repository.RoleRepository;
import com.ecommerce.user_service.repository.UserRepository;
import com.ecommerce.user_service.security.CookieUtil;
import com.ecommerce.user_service.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_RESEND_PER_HOUR = 3;
    private static final String REFRESH_TOKEN_PREFIX   = "refresh::token::";
    private static final String TOKEN_BLACKLIST_PREFIX = "token::blacklist::";


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        log.debug("Loading user by username or email: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username or email: " + usernameOrEmail
                ));

        List<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

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


    // ════════════════════════════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {

        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        // Step 1: Load user by username or email
        User user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid username/email or password"
                ));

        // Step 2: Check account suspended
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AccountSuspendedException(
                    "Your account has been suspended. Please contact support."
            );
        }

        // Step 3: Check email verified
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in. Check your inbox for the OTP."
            );
        }

        // Step 4: Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username/email or password");
        }

        // Step 5: Get roles as list
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Step 6: Generate access token (15 min)
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getUsername(),
                roles
        );

        // Step 7: Generate refresh token (7 days) → store in Redis
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        storeRefreshTokenInRedis(user.getId(), refreshToken);

        // Step 8: Set both tokens as HttpOnly cookies
        cookieUtil.setAccessTokenCookie(response, accessToken);
        cookieUtil.setRefreshTokenCookie(response, refreshToken);

        // Step 9: Update lastLoginAt
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("Login successful for user: {} | id: {}", user.getUsername(), user.getId());

        // Step 10: Return AuthResponse (tokens are in cookies — not in body)
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .roles(roles.stream().collect(Collectors.toSet()))
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getJwt().getAccessTokenExpiryMs() / 1000)
                .build();
    }

    // ════════════════════════════════════════════════════════════════
    // LOGOUT
    // ════════════════════════════════════════════════════════════════
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        log.info("Logout request received");

        // Step 1: Get access token from cookie
        String accessToken = cookieUtil
                .getCookieValue(request, jwtProperties.getCookie().getAccessTokenName())
                .orElse(null);

        if (accessToken != null && jwtUtil.validateToken(accessToken)) {

            String userId    = jwtUtil.extractUserId(accessToken);
            String tokenId   = jwtUtil.extractTokenId(accessToken);
            long   expiryMs  = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();

            // Step 2: Blacklist access token in Redis until it expires
            if (expiryMs > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + tokenId,
                        userId,
                        expiryMs,
                        TimeUnit.MILLISECONDS
                );
            }

            // Step 3: Delete refresh token from Redis
            deleteRefreshTokenFromRedis(userId);

            log.info("User logged out: {} | token blacklisted", userId);
        }

        // Step 4: Clear both cookies
        cookieUtil.clearAccessTokenCookie(response);
        cookieUtil.clearRefreshTokenCookie(response);

        log.info("Cookies cleared successfully");
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════

    private void storeRefreshTokenInRedis(String userId, String refreshToken) {
        long ttlMs = jwtProperties.getJwt().getRefreshTokenExpiryMs();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                ttlMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("Refresh token stored in Redis for userId: {}", userId);
    }

    private void deleteRefreshTokenFromRedis(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.debug("Refresh token deleted from Redis for userId: {}", userId);
    }
}