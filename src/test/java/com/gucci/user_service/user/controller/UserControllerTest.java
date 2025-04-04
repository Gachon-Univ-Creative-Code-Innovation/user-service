package com.gucci.user_service.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import com.gucci.user_service.user.dto.VerifyCheckRequest;
import com.gucci.user_service.user.dto.VerifySendRequest;
import com.gucci.user_service.user.service.EmailVerificationService;
import com.gucci.user_service.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public EmailVerificationService emailVerificationService() {
            return Mockito.mock(EmailVerificationService.class);
        }
    }

    @Nested
    @DisplayName("회원가입 API (/api/user/signup)")
    class SignUpTests {

        @Test
        @DisplayName("성공: 유효한 요청일 때 201 응답과 유저 정보 반환")
        void signUpSuccess() throws Exception {
            // given
            SignUpDtoRequest request = new SignUpDtoRequest();
            request.setEmail("test@example.com");
            request.setName("테스트유저");
            request.setNickname("tester");
            request.setPassword("password123");
            request.setRole("USER");

            User savedUser = User.builder()
                    .userId(1L)
                    .email(request.getEmail())
                    .name(request.getName())
                    .nickname(request.getNickname())
                    .password("encodedPassword")
                    .role(Role.USER)
                    .build();

            Mockito.when(userService.signUp(Mockito.any(SignUpDtoRequest.class)))
                    .thenReturn(savedUser);

            // when & then
            mockMvc.perform(post("/api/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("테스트유저님 회원가입 완료"))
                    .andExpect(jsonPath("$.data.userId").value(1));
        }

        @Test
        @DisplayName("실패: 이메일 형식이 잘못되었을 때 400 응답")
        void signUpInvalidEmail() throws Exception {
            // given
            SignUpDtoRequest request = new SignUpDtoRequest();
            request.setEmail("invalid-email"); // 잘못된 이메일
            request.setName("테스트유저");
            request.setNickname("tester");
            request.setPassword("password123");
            request.setRole("USER");

            // when & then
            mockMvc.perform(post("/api/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 필수 필드가 빠졌을 때 400 응답")
        void signUpMissingFields() throws Exception {
            // given
            SignUpDtoRequest request = new SignUpDtoRequest(); // 아무 값도 없음

            // when & then
            mockMvc.perform(post("/api/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("헬스 체크 API (/api/user/health-check)")
    class HealthCheckTests {
        @Test
        @DisplayName("성공: 서비스가 실행 중일 때 200 응답")
        void healthCheck() throws Exception {
            mockMvc.perform(get("/api/user/health-check"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("이메일 중복 확인 API (/api/user/check-email/{email})")
    class EmailCheckTests {

        @Test
        @DisplayName("성공: 중복되지 않은 이메일일 때 false 반환")
        void emailNotDuplicated() throws Exception {
            String email = "unique@example.com";
            Mockito.when(userService.isEmailDuplicated(email)).thenReturn(false);

            mockMvc.perform(get("/api/user/check-email/" + email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이메일 중복 확인"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("성공: 중복된 이메일일 때 true 반환")
        void emailDuplicated() throws Exception {
            String email = "duplicate@example.com";
            Mockito.when(userService.isEmailDuplicated(email)).thenReturn(true);

            mockMvc.perform(get("/api/user/check-email/" + email))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이메일 중복 확인"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("인증코드 전송 API (/api/user/verify/send)")
    class SendVerificationCodeTests {

        @Test
        @DisplayName("성공: 이메일 형식이 유효할 때 인증 코드 전송")
        void sendVerificationCodeSuccess() throws Exception {
            VerifySendRequest request = new VerifySendRequest("test@example.com");

            mockMvc.perform(post("/api/user/verify/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("인증 코드가 전송되었습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("실패: 이메일 형식이 잘못되었을 때 400 반환")
        void sendVerificationCodeInvalidEmail() throws Exception {
            VerifySendRequest request = new VerifySendRequest("invalid-email");

            mockMvc.perform(post("/api/user/verify/send")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("인증코드 확인 API (/api/user/verify/check)")
    class CheckVerificationCodeTests {

        @Test
        @DisplayName("성공: 인증코드가 일치할 때")
        void verifyCodeSuccess() throws Exception {
            VerifyCheckRequest request = new VerifyCheckRequest("test@example.com", "123456");
            Mockito.when(emailVerificationService.verifyCode("test@example.com", "123456")).thenReturn(true);

            mockMvc.perform(post("/api/user/verify/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("이메일 인증 성공"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("실패: 잘못된 요청 형식")
        void verifyCodeInvalidRequest() throws Exception {
            VerifyCheckRequest request = new VerifyCheckRequest("invalid-email", ""); // invalid

            mockMvc.perform(post("/api/user/verify/check")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}