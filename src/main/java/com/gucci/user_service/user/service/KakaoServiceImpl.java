package com.gucci.user_service.user.service;

import com.gucci.user_service.user.dto.AccessTokenDto;
import com.gucci.user_service.user.dto.KakaoProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class KakaoServiceImpl implements KakaoService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;



    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public AccessTokenDto getAccessToken(String code) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                //?code=xxxx&client_id=xxxx&client_secret=xxxx&redirect_uri=xxxx&grant_type=authorization_code
                .body(params)
                //retrieve()는 응답 body값만을 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        log.info("응답 AccessToken JSON: "+ response.getBody());

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
