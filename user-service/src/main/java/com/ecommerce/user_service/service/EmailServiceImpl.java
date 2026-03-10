package com.ecommerce.user_service.service;

import com.ecommerce.user_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    // ── Send Verification Email ──────────────────────────────────────
    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String firstName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email — OTP: " + otp);
            helper.setText(buildVerificationEmailBody(firstName, otp, toEmail), true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {} | Error: {}", toEmail, e.getMessage());
        }
    }

    // ── Resend Verification Email ────────────────────────────────────
    @Async
    @Override
    public void sendResendVerificationEmail(String toEmail, String firstName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New OTP — Verify Your Email: " + otp);
            helper.setText(buildVerificationEmailBody(firstName, otp, toEmail), true);

            mailSender.send(message);
            log.info("Resend verification email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to resend verification email to: {} | Error: {}", toEmail, e.getMessage());
        }
    }

    // ── Email HTML Body ──────────────────────────────────────────────
    public String buildVerificationEmailBody(String firstName, String otp, String email) {
        String verifyUrl = baseUrl + "/api/v1/auth/verify-email?token=" + otp + "&email=" + email;

        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                <h2 style="color: #1E3A5F;">Email Verification</h2>
                <p>Hi erripukka <strong>%s</strong>,</p>
                <p>Thank you for registering. Use the OTP below to verify your email address.</p>

                <div style="background:#f4f4f4; padding:20px; text-align:center; border-radius:8px; margin:20px 0;">
                    <h1 style="letter-spacing:10px; color:#1565C0;">%s</h1>
                    <p style="color:#888;">This OTP expires in <strong>10 minutes lo kompala medha vastharu </strong></p>
                </div>

                <p>Or click the button below:</p>
                <a href="%s"
                   style="background:#1565C0; color:white; padding:12px 24px;
                          text-decoration:none; border-radius:6px; display:inline-block;">
                   Verify Email
                </a>

                <p style="margin-top:30px; color:#888; font-size:12px;">
                    If you did not create an account, please ignore this email.
                </p>
            </body>
            </html>
        """.formatted(firstName, otp, verifyUrl);
    }
    // ── Password Reset Email HTML Body ───────────────────────────────
    private String buildPasswordResetEmailBody(String firstName, String otp) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                <h2 style="color: #1E3A5F;">Reset Your Password</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>We received a request to reset your password. Use the OTP below to reset it.</p>

                <div style="background:#f4f4f4; padding:20px; text-align:center; border-radius:8px; margin:20px 0;">
                    <h1 style="letter-spacing:10px; color:#C62828;">%s</h1>
                    <p style="color:#888;">This OTP expires in <strong>10 minutes</strong></p>
                </div>

                <p style="margin-top:20px; color:#555;">
                    Enter this OTP in the app to reset your password.
                </p>

                <p style="margin-top:30px; color:#888; font-size:12px;">
                    If you did not request a password reset, please ignore this email.
                    Your password will not be changed.
                </p>
            </body>
            </html>
        """.formatted(firstName, otp);
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String firstName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password — OTP: " + otp);
            helper.setText(buildPasswordResetEmailBody(firstName, otp), true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {} | Error: {}", toEmail, e.getMessage());
        }
    }
}