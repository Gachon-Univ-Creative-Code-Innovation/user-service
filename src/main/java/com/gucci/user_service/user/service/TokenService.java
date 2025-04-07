package com.gucci.user_service.user.service;

public interface TokenService {
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMinutes);
    public String getRefreshToken(Long userId);
    public void deleteRefreshToken(Long userId);
    public boolean validateRefreshToken(Long userId, String token);
    }
