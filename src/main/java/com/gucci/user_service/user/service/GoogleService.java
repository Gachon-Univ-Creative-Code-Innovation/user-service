package com.gucci.user_service.user.service;

import com.gucci.user_service.user.dto.AccessTokenDto;
import com.gucci.user_service.user.dto.GoogleProfileDto;

public interface GoogleService {
    public AccessTokenDto getAccessToken(String code);

    public GoogleProfileDto getProfile(String token);
}
