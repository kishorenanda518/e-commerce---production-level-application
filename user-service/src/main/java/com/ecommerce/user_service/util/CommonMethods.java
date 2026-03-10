package com.ecommerce.user_service.util;

import com.ecommerce.user_service.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonMethods {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties                 jwtProperties;

    public static final String REFRESH_TOKEN_PREFIX        = "refresh::token::";
    public static final String TOKEN_BLACKLIST_PREFIX      = "token::blacklist::";
    public static final String FORGOT_PASSWORD_OTP_PREFIX  = "password::reset::";
    public static final String FORGOT_PASSWORD_COUNT_PREFIX = "password::reset::count::";

    public void storeRefreshToken(String userId, String refreshToken) {
        long ttlMs = jwtProperties.getJwt().getRefreshTokenExpiryMs();
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                ttlMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("Refresh token stored in Redis for userId: {}", userId);
    }

    // ── Delete Refresh Token ─────────────────────────────────────────
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.debug("Refresh token deleted from Redis for userId: {}", userId);
    }

    // ── Blacklist Access Token ───────────────────────────────────────
    public void blacklistAccessToken(String tokenId, String userId, long expiryMs) {
        if (expiryMs > 0) {
            redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + tokenId,
                    userId,
                    expiryMs,
                    TimeUnit.MILLISECONDS
            );
            log.debug("Access token blacklisted for userId: {}", userId);
        }
    }

    // ── Store Forgot Password OTP ────────────────────────────────────
    public void storeForgotPasswordOtp(String email, String otp) {
        redisTemplate.opsForValue().set(
                FORGOT_PASSWORD_OTP_PREFIX + email,
                otp,
                10,
                TimeUnit.MINUTES
        );
        log.debug("Forgot password OTP stored in Redis for email: {}", email);
    }

    // ── Get Forgot Password OTP ──────────────────────────────────────
    public String getForgotPasswordOtp(String email) {
        return redisTemplate.opsForValue().get(FORGOT_PASSWORD_OTP_PREFIX + email);
    }

    // ── Delete Forgot Password OTP ───────────────────────────────────
    public void deleteForgotPasswordOtp(String email) {
        redisTemplate.delete(FORGOT_PASSWORD_OTP_PREFIX + email);
        log.debug("Forgot password OTP deleted from Redis for email: {}", email);
    }

    // ── Increment Forgot Password Count ─────────────────────────────
    public int incrementForgotPasswordCount(String email) {
        String key = FORGOT_PASSWORD_COUNT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
        return count != null ? count.intValue() : 1;
    }

    // ── Get Forgot Password Count ────────────────────────────────────
    public int getForgotPasswordCount(String email) {
        String value = redisTemplate.opsForValue().get(FORGOT_PASSWORD_COUNT_PREFIX + email);
        return value != null ? Integer.parseInt(value) : 0;
    }

    // ── Delete Forgot Password Count ─────────────────────────────────
    public void deleteForgotPasswordCount(String email) {
        redisTemplate.delete(FORGOT_PASSWORD_COUNT_PREFIX + email);
    }
}