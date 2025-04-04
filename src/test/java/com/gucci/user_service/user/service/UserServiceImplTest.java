package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import com.gucci.user_service.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignUp() {
        SignUpDtoRequest signUpDtoRequest = new SignUpDtoRequest();
        signUpDtoRequest.setEmail("test@example.com");
        signUpDtoRequest.setName("Test User");
        signUpDtoRequest.setNickname("testuser");
        signUpDtoRequest.setPassword("password123");
        signUpDtoRequest.setRole("USER");

        User user = User.builder()
                .email(signUpDtoRequest.getEmail())
                .role(Role.USER)
                .name(signUpDtoRequest.getName())
                .nickname(signUpDtoRequest.getNickname())
                .password("encodedPassword")
                .build();

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.signUp(signUpDtoRequest);

        assertNotNull(createdUser);
    }
}