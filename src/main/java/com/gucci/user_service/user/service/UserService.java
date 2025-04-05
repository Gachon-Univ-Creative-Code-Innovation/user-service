package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.LoginDtoRequest;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import jakarta.validation.Valid;

public interface UserService {
    public User signUp(SignUpDtoRequest signUpDtoRequest);

    public boolean isEmailDuplicated(String email);

    User login(LoginDtoRequest loginDTORequest);

    public User getUserBySocialId(String socialId);
    public User createOauth(String socialId, String email, String name,SocialType socialType, String profileUrl);
    }
