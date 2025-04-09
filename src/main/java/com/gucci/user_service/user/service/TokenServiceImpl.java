package com.gucci.user_service.user.service;

import com.gucci.user_service.user.config.security.auth.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

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

    @Override
    public boolean isValidRefreshToken(String refreshToken, Long userId) {
        // Redis에서 모든 키를 검색하여 refreshToken이 존재하는지 확인
        String refreshTokenFromRedis = getRefreshToken(userId);



        return refreshTokenFromRedis != null && refreshTokenFromRedis.equals(refreshToken);


    }
}
