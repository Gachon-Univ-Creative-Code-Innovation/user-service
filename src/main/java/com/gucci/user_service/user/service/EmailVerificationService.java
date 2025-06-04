package com.gucci.user_service.user.service;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import com.gucci.user_service.user.service.util.CodeGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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


            // 이메일 메시지 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("[AlOG] 회원가입 인증번호 안내");
            helper.setText(createEmailContent(code), true); // true는 HTML 형식 사용을 의미
            helper.setFrom(fromAddress);

            mailSender.send(message);
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}: {}", email, e.getMessage());
            throw new CustomException(ErrorCode.FAIL_SEND_CODE);
        }
    }

    public boolean verifyCode(String email, String inputCode) {
        String key = "email:verify:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            log.warn("No verification code found for email {}", email);
            throw new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND);
        }
        boolean isValid = inputCode.equals(storedCode);
        if (isValid) {
            log.info("Verification code for email {} is valid", email);
        } else {
            log.warn("Invalid verification code for email {}", email);
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        return isValid;
    }

    // 이메일 본문 HTML을 생성
    private String createEmailContent(String code) {
        return String.format("""
            <div style='text-align: center; margin: 30px;'>
                <h3> AlOG </h3>
                <h2>이메일 인증 코드</h2>
                <p> 본 메일은 AlOG 회원가입을 위한 이메일 인증입니다.</p>
                <p> 아래의 인증 코드를 입력하여 본인확인을 해주시기 바랍니다.</p>
                <div style='font-size: 24px; font-weight: bold; margin: 20px;'>%s</div>
                <p>이 코드는 5분 동안 유효합니다.</p>
            </div>
            """, code);
    }

    public void sendResetPasswordEmail(String email, String token){
        String resetLink = "https://a-log.site/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[AlOG] 비밀번호 재설정 안내");
            helper.setText(createResetPasswordContent(resetLink), true);
            helper.setFrom(fromAddress);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("비밀번호 재설정 메일 전송 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String createResetPasswordContent(String resetLink) {
        return String.format("""
        <div style='text-align: center; margin: 30px; font-family: Arial, sans-serif;'>
            <h3 style='color: #4CAF50;'>AlOG</h3>
            <h2>비밀번호 재설정 안내</h2>
            <p>요청하신 이메일 계정에 대해 비밀번호 재설정 절차를 진행합니다.</p>
            <p>아래 버튼을 클릭하여 비밀번호를 재설정하세요.</p>
            <a href='%s' 
               style='display: inline-block; margin: 20px 0; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px;'>
               비밀번호 재설정
            </a>
            <p style='font-size: 14px; color: #888;'>이 링크는 15분간 유효합니다.</p>
            <p style='font-size: 12px; color: #aaa;'>본인이 요청하지 않은 경우 이 메일을 무시해 주세요.</p>
        </div>
    """, resetLink);
    }
}