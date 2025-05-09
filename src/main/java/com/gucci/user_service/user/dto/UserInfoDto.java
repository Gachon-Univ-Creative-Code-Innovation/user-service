package com.gucci.user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoDto {
    private String email;
    private String name;
    private String nickname;
    private String profileUrl;
    private String githubUrl;
}