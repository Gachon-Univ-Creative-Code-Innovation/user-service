package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.*;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface UserService {
    public User signUp(SignUpDtoRequest signUpDtoRequest);

    public boolean isEmailDuplicated(String email);

    User login(LoginDtoRequest loginDTORequest);

    Boolean isNicknameDuplicated(String nickname);
    public User getUserBySocialId(String socialId);
    public User createOauth(String socialId, String name,SocialType socialType, String profileUrl);
    UserInfoDto getUserInfoById(Long userId);
    void updateUser(Long userId, UpdateUserDtoRequest updateUserDtoRequest);

    MainUserInfoDto getMainUserInfo(Long userId);

    Map<Long, String> getProfileByIds(List<Long> targetIds);

    Map<Long, String> getNicknameByIds(List<Long> targetIds);

    String getNickname(Long userId);

    void sendResetPasswordEmail(String email);

    void resetPassword(ResetPasswordRequestDto request);

    Map<String, String> getProfileUrlAndNickname(Long userId);

    Map<String, String> getUserDetails(Long userId);
}
