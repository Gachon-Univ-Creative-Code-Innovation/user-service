package com.gucci.user_service.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import com.gucci.user_service.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SignUpIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @Transactional
    @Rollback
    @DisplayName("회원가입 정상 요청 시 201 응답과 사용자 저장 여부 확인")
    void signUp_whenValidRequest_thenReturns201AndSavesUser() throws Exception {
        // given
        SignUpDtoRequest request = new SignUpDtoRequest();
        request.setEmail("integration@test.com");
        request.setName("통합테스트");
        request.setNickname("integTester");
        request.setPassword("securePassword123");
        request.setRole("USER");

        String url = "http://localhost:" + port + "/api/user/signup";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        // when
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        User savedUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo(request.getName());
        assertThat(savedUser.getNickname()).isEqualTo(request.getNickname());
        assertThat(savedUser.getRole().name()).isEqualTo(request.getRole());
    }
}