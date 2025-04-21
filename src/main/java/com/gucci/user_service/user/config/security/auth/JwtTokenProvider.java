package com.gucci.user_service.user.config.security.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;


@Component
public class JwtTokenProvider {

    private final String secretKey;
    private final int accessExpiration;     // 분 단위
    private final int refreshExpiration;    // 분 단위
    private final Key SECRET_KEY;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-expiration}") int accessExpiration,
            @Value("${jwt.refresh-expiration}") int refreshExpiration
    ) {
        this.secretKey = secretKey;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.SECRET_KEY = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretKey),
                SignatureAlgorithm.HS512.getJcaName()
        );
    }
    // Access Token
    public String createAccessToken(String email, Long userId, String role, String nickname) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("user_id", userId);
        claims.put("role", role);
        claims.put("nickname", nickname);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessExpiration * 60 * 1000L))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Refresh Token
    public String createRefreshToken(String email, Long userId, String role, String nickname) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("user_id", userId);
        claims.put("role", role);
        claims.put("nickname", nickname);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshExpiration * 60 * 1000L))
                .signWith(SECRET_KEY)
                .compact();
    }
    public int getRefreshExpiration() {
        return refreshExpiration;
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return extractClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return extractClaims(token).get("user_id", Long.class);
    }

    public String getRoleFromToken(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String getNicknameFromToken(String token) {
        return extractClaims(token).get("nickname", String.class);
    }
}