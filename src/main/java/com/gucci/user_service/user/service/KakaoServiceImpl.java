package com.gucci.user_service.user.service;

import com.gucci.user_service.user.dto.AccessTokenDto;
import com.gucci.user_service.user.dto.KakaoProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class KakaoServiceImpl implements KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;



    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public AccessTokenDto getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<AccessTokenDto> response = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, AccessTokenDto.class
        );

        System.out.println("응답 AccessToken JSON: " + response.getBody());

        return  response.getBody();    }

    @Override
    public KakaoProfileDto getKakaoProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<KakaoProfileDto> response = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(KakaoProfileDto.class);
        log.info("profile JSON"+ response.getBody());


        return response.getBody();
    }
}
