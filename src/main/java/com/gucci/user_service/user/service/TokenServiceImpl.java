package com.gucci.user_service.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
    @Override
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMinutes) {
       String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofMinutes(expirationMinutes));
        log.info("Saved refresh token for userId {} with expiration {} min", userId, expirationMinutes);


    }

    @Override
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
    @Override
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Deleted refresh token for userId {}", userId);
    }

    @Override
    public boolean validateRefreshToken(Long userId, String token) {
        String stored = getRefreshToken(userId);
        return stored != null && stored.equals(token);
    }
}
