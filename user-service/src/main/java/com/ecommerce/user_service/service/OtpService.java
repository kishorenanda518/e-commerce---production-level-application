package com.ecommerce.user_service.service;

public interface OtpService {
    String generateAndStoreOtp(String email);
    boolean validateOtp(String email, String otp);
    void deleteOtp(String email);
    int incrementResendCount(String email);
    int getResendCount(String email);
}
