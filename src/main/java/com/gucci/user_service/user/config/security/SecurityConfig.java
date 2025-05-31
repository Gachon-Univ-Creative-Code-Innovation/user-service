package com.gucci.user_service.user.config.security;

import com.gucci.user_service.user.config.security.auth.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
//                .cors(Customizer.withDefaults())
                .cors(AbstractHttpConfigurer::disable) //Gateway에서 CORS 설정
                .csrf(AbstractHttpConfigurer::disable) //csrf 비활성화
                //Basic 인증 비활성화
                //Basic 인증은 사용자 이름과 비밀번호를 Base64로 인코딩하여 인증값으로 활용
                .httpBasic(AbstractHttpConfigurer::disable)
                //세션방식을 비활성화
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //특정 url 패턴에 대해서는 인증처리(Authentication)를 제외
                .authorizeHttpRequests(
                        a->a
                                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                                .requestMatchers(
                                        "/api/user-service/signup",
                                        "/api/user-service/health-check",
                                        "/api/user-service/check-email/**",
                                        "/api/user-service/check-nickname/**",
                                        "/api/user-service/signin",
                                        "/api/user-service/verify/**",
                                        "/api/user-service/google/login",
                                        "/api/user-service/kakao/login",
                                        "/api/user-service/refresh-token",
                                        "/api/user-service/reset-password",
                                        "/api/user-service/reset-password-request",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/actuator/prometheus"
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                //UsernamePasswordAuthenticationFilter 이 클래스에서 폼로그인 인증을 처리
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
