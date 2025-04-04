package com.gucci.user_service.user.service;


import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoRequest;

public interface UserService {
    public User signUp(SignUpDtoRequest signUpDtoRequest);

    public boolean isEmailDuplicated(String email);
}
