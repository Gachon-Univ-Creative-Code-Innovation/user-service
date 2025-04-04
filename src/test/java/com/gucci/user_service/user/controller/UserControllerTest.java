package com.gucci.user_service.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoReq;
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
            SignUpDtoReq request = new SignUpDtoReq();
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

            Mockito.when(userService.signUp(Mockito.any(SignUpDtoReq.class)))
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
            SignUpDtoReq request = new SignUpDtoReq();
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
            SignUpDtoReq request = new SignUpDtoReq(); // 아무 값도 없음

            // when & then
            mockMvc.perform(post("/api/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}