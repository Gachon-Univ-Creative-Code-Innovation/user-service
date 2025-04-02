package com.gucci.user_service.user.service;

import com.gucci.user_service.user.config.error.CustomException;
import com.gucci.user_service.user.service.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${spring.mail.username}") // <- 여기 추가!
    private String fromAddress;

    private static final long EXPIRATION_MINUTES = 5;

    public void sendVerificationCode(String email) {
        String code = CodeGenerator.generateCode();

        try {
            // Store in Redis (valid for 5 minutes)
            String key = "email:verify:" + email;
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(EXPIRATION_MINUTES));

            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Email Verification Code");
            message.setText("Verification code: " + code);

// ❗ 중요: 본인 계정과 정확히 일치해야 함
            message.setFrom(fromAddress); // <- 여기 사용!

            mailSender.send(message);
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}: {}", email, e.getMessage());
            throw new CustomException("Failed to send verification code", 500);
        }
    }

    public boolean verifyCode(String email, String inputCode) {
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            log.warn("No verification code found for email {}", email);
            throw new CustomException("No verification code found", 404);
        }
        boolean isValid = inputCode.equals(storedCode);
        if (isValid) {
            log.info("Verification code for email {} is valid", email);
        } else {
            log.warn("Invalid verification code for email {}", email);
            throw new CustomException("Invalid verification code", 400);
        }
        return isValid;
    }
}