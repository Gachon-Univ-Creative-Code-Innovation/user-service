package com.gucci.user_service.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)// 없는 필드는 무시
public class KakaoProfileDto {
    private String id;
    private KakaoAccount kakao_account;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)// 없는 필드는 무시
    public static  class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)// 없는 필드는 무시
    public static class Profile {
        private String nickname;
        private String profile_image_url;
    }
}