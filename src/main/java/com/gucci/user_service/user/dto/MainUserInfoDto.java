package com.gucci.user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MainUserInfoDto {
    private String nickname;
    private String profileUrl; // 변수명 수정 (ProfileUrl -> profileUrl)
    private Integer followers;
    private Integer following;

    // JPQL에서 사용되는 생성자 추가
    public MainUserInfoDto(String nickname, String profileUrl, Long followers, Long following) {
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.followers = followers != null ? followers.intValue() : 0;
        this.following = following != null ? following.intValue() : 0;
    }
}