package com.gucci.user_service.user.service;

import com.gucci.user_service.user.dto.AccessTokenDto;
import com.gucci.user_service.user.dto.KakaoProfileDto;

public interface KakaoService {
    public AccessTokenDto getAccessToken(String code);
    public KakaoProfileDto getKakaoProfile(String token);
    }
