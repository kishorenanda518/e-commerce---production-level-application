package com.ecommerce.user_service.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String firstName, String otp);
    void sendResendVerificationEmail(String toEmail, String firstName, String otp);
    void sendPasswordResetEmail(String toEmail, String firstName, String otp);
}
