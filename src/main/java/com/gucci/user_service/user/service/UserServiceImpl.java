package com.gucci.user_service.user.service;

import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.LoginDtoRequest;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import com.gucci.user_service.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );


    @Override
    public User signUp(SignUpDtoRequest signUpDtoRequest) {
        return userRepository.save(
                User.builder()
                        .email(signUpDtoRequest.getEmail())
                        .role(Role.valueOf(signUpDtoRequest.getRole()))
                        .name(signUpDtoRequest.getName())
                        .nickname(signUpDtoRequest.getNickname())
                        .password(passwordEncoder.encode(signUpDtoRequest.getPassword()))
                .build());
    }

    @Override
    public boolean isEmailDuplicated(String email) {

                if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일이 제공되지 않았습니다.");
        }
        if (!EMAIL_REGEX.matcher(email).matches()) {

            throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
        }
        return userRepository.existsByEmail(email);
    }

    @Override
    public User login(LoginDtoRequest loginDTORequest) {
        Optional<User> optUser = userRepository.findByEmail(loginDTORequest.getEmail());
        if(!optUser.isPresent()){
            throw new IllegalArgumentException("이메일을 확인해주세요.");
        }
        User user = optUser.get();
        if(!passwordEncoder.matches(loginDTORequest.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        return user;

    }

    @Override
    public User getUserBySocialId(String socialId) {
        User user = userRepository.findBySocialId(socialId).orElse(null);
        return user;
    }

    @Override
    public User createOauth(String socialId, String email, String name,SocialType socialType, String profileUrl) {
        User user = User.builder()
                .email(email)
                .name(name)
                .socialType(socialType)
                .socialId(socialId)
                .profileUrl(profileUrl)
                .role(Role.USER)
                .build();


        return userRepository.save(user);
    }

    @Override
    public Boolean isNicknameDuplicated(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임이 제공되지 않았습니다.");
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2~20자 사이여야 합니다.");
        }


        return userRepository.existsByNickname(nickname);
    }
}
