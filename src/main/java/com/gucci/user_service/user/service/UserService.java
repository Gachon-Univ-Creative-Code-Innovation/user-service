package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoReq;

public interface UserService {
    public User signUp(SignUpDtoReq signUpDtoReq);
}
