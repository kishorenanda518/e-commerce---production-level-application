package com.ecommerce.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    // ── Redis Key Patterns ───────────────────────────────────────────
    private static final String OTP_KEY_PREFIX       = "email::otp::";
    private static final String RESEND_COUNT_PREFIX  = "email::resend::count::";

    // ── TTL Config ───────────────────────────────────────────────────
    private static final long OTP_TTL_MINUTES        = 10;   // OTP expires in 10 min
    private static final long RESEND_WINDOW_MINUTES  = 60;   // Resend counter resets every 1 hour

    private final SecureRandom secureRandom = new SecureRandom();

    // ────────────────────────────────────────────────────────────────
    @Override
    public String generateAndStoreOtp(String email) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", secureRandom.nextInt(999999));

        String key = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("OTP generated and stored for email: {} | TTL: {} min", email, OTP_TTL_MINUTES);
        return otp;
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public boolean validateOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("OTP expired or not found for email: {}", email);
            return false;
        }

        boolean isValid = storedOtp.equals(otp);
        if (!isValid) {
            log.warn("OTP mismatch for email: {}", email);
        }
        return isValid;
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void deleteOtp(String email) {
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.delete(key);
        log.info("OTP deleted from Redis for email: {}", email);
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public int incrementResendCount(String email) {
        String key = RESEND_COUNT_PREFIX + email;

        Long count = redisTemplate.opsForValue().increment(key);

        // Set TTL only on first increment (when count == 1)
        if (count != null && count == 1) {
            redisTemplate.expire(key, RESEND_WINDOW_MINUTES, TimeUnit.MINUTES);
            log.info("Resend counter started for email: {} | Window: {} min", email, RESEND_WINDOW_MINUTES);
        }

        return count != null ? count.intValue() : 1;
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public int getResendCount(String email) {
        String key = RESEND_COUNT_PREFIX + email;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
}