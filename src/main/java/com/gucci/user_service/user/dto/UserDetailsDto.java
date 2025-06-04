package com.gucci.user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
    private String name;
    private String profileUrl;
    private String githubUrl;
    private String nickname;
    private String email;
}