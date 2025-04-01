package com.gucci.user_service.user.service;

import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoReq;
import com.gucci.user_service.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User signUp(SignUpDtoReq signUpDtoReq) {
        return userRepository.save(
                User.builder()
                        .email(signUpDtoReq.getEmail())
                        .role(Role.valueOf(signUpDtoReq.getRole()))
                        .name(signUpDtoReq.getName())
                        .nickname(signUpDtoReq.getNickname())
                        .password(passwordEncoder.encode(signUpDtoReq.getPassword()))
                .build());
    }
}
