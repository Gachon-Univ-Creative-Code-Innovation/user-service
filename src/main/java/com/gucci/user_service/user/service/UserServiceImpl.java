package com.gucci.user_service.user.service;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import com.gucci.user_service.follow.repository.FollowRepository;
import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.config.error.UserNotFoundException;
import com.gucci.user_service.user.config.security.auth.JwtTokenProvider;
import com.gucci.user_service.user.domain.Role;
import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.*;
import com.gucci.user_service.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwsS3Service awsS3Service;
    private final FollowRepository followRepository;
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;
    private final RedisTemplate<Object, Object> redisTemplate;


    @Override
    public User signUp(SignUpDtoRequest signUpDtoRequest) {

        // githubUsername이 없을 경우 기본값 설정
        String githubUrl = (signUpDtoRequest.getGithubUsername() != null && !signUpDtoRequest.getGithubUsername().isEmpty())
                ? "https://github.com/" + signUpDtoRequest.getGithubUsername()
                : null;

        // 프로필 사진이 있을 경우 업로드 처리
        if (signUpDtoRequest.getProfileImage() != null && !signUpDtoRequest.getProfileImage().isEmpty()) {
            String newProfileUrl = awsS3Service.uploadFile(signUpDtoRequest.getProfileImage());
            return userRepository.save(
                    User.builder()
                            .email(signUpDtoRequest.getEmail())
                            .role(Role.valueOf(signUpDtoRequest.getRole()))
                            .name(signUpDtoRequest.getName())
                            .nickname(signUpDtoRequest.getNickname())
                            .password(passwordEncoder.encode(signUpDtoRequest.getPassword()))
                            .githubUrl(githubUrl)
                            .profileUrl(newProfileUrl)
                            .build());
        }

        // 프로필 사진이 없을 경우 기본값 설정
        return userRepository.save(
                User.builder()
                        .email(signUpDtoRequest.getEmail())
                        .role(Role.valueOf(signUpDtoRequest.getRole()))
                        .name(signUpDtoRequest.getName())
                        .nickname(signUpDtoRequest.getNickname())
                        .password(passwordEncoder.encode(signUpDtoRequest.getPassword()))
                        .githubUrl(githubUrl)
                        .profileUrl(null) // 기본 프로필 URL이 있다면 설정 가능
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
    public UserInfoDto getUserInfoById(Long userId) {
        UserInfoDto userInfo = userRepository.findUserInfoById(userId);
        if (userInfo == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return userInfo;
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


    @Override
    public void updateUser(Long userId, UpdateUserDtoRequest updateUserDtoRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (updateUserDtoRequest.getName() != null) {
            user.setName(updateUserDtoRequest.getName());
        }

        if (updateUserDtoRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateUserDtoRequest.getPassword()));
        }
        if (updateUserDtoRequest.getGithubUrl() != null) {
            user.setGithubUrl(updateUserDtoRequest.getGithubUrl());
        }
        if (updateUserDtoRequest.getNickname() != null) {
            if (userRepository.existsByNickname(updateUserDtoRequest.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(updateUserDtoRequest.getNickname());
        }

        // 프로필 사진 업데이트
        if (updateUserDtoRequest.getProfileImage() != null && !updateUserDtoRequest.getProfileImage().isEmpty()) {
            // 소셜 로그인 사용자가 아닌 경우에만 기존 프로필 URL 삭제
            if (user.getProfileUrl() != null && user.getSocialType() == null) {
                awsS3Service.deleteFile(user.getProfileUrl());
            }
            // 새 프로필 사진 업로드
            String newProfileUrl = awsS3Service.uploadFile(updateUserDtoRequest.getProfileImage());
            user.setProfileUrl(newProfileUrl);
        }

        userRepository.save(user);
    }

    @Override
    public MainUserInfoDto getMainUserInfo(Long userId) {
        return userRepository.findMainUserInfoById(userId);
    }

    @Override
    public Map<Long, String> getNicknameByIds(List<Long> targetIds) {
        List<Object[]> userIdAndNickname = userRepository.findUserIdAndNicknameByIdIn(targetIds);

        return userIdAndNickname.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));
    }

    @Override
    public String getNickname(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.getNickname();
    }

    @Override
    public void sendResetPasswordEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        String token = jwtTokenProvider.passwordResetToken(email);
        redisTemplate.opsForValue().set("reset:token:"+token,email, Duration.ofMinutes(15));

        emailVerificationService.sendResetPasswordEmail(email,token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        Object emailObj = redisTemplate.opsForValue().get("reset:token:" + request.getToken());
        if (emailObj == null) {
            throw new CustomException(ErrorCode.EXPIRATION_TOKEN);
        }

        String email = emailObj.toString();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        redisTemplate.delete("reset:token:" + request.getToken());
    }

}
