package com.gucci.user_service.user.service;

import com.gucci.user_service.user.dto.AccessTokenDto;
import com.gucci.user_service.user.dto.GoogleProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GoogleServiceImpl implements GoogleService {
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;
    @Override
    public AccessTokenDto getAccessToken(String code) {
        //인가 코드, Client Id, Client Secret, redirect uri, grant_type
        //Spring6부터 RestTemplate 비추천 상태이기 떄문에, 대신 RestClient 사용
        RestClient restClient = RestClient.create();

        //MultiValueMap을 통해 자동으로 form-data 형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                //?code=xxxx&client_id=xxxx&client_secret=xxxx&redirect_uri=xxxx&grant_type=authorization_code
                .body(params)
                //retrieve()는 응답 body값만을 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        log.info("응답 AccessToken JSON: "+ response.getBody());

        System.out.println("응답 AccessToken JSON: " + response);

        return  response.getBody();
    }

    @Override
    public GoogleProfileDto getProfile(String token) {
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileDto> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);
        log.info("profile JSON"+ response.getBody());


        return response.getBody();    }
}
