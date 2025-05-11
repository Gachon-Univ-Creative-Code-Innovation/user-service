package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface UserService {
    public User signUp(SignUpDtoRequest signUpDtoRequest);

    public boolean isEmailDuplicated(String email);

    User login(LoginDtoRequest loginDTORequest);

    Boolean isNicknameDuplicated(String nickname);
    public User getUserBySocialId(String socialId);
    public User createOauth(String socialId, String email, String name,SocialType socialType, String profileUrl);
    UserInfoDto getUserInfoById(Long userId);
    void updateUser(Long userId, UpdateUserDtoRequest updateUserDtoRequest);

    MainUserInfoDto getMainUserInfo(Long userId);

    Map<Long, String> getNicknameByIds(List<Long> targetIds);

    String getNickname(Long userId);
}
