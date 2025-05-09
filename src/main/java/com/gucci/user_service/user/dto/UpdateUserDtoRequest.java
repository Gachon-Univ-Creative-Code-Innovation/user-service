package com.gucci.user_service.user.dto;

import com.gucci.user_service.user.config.error.EnumValid;
import com.gucci.user_service.user.domain.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateUserDtoRequest {
    @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
    private String name;



    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Pattern(
            regexp = "^(https:\\/\\/github\\.com\\/)[a-zA-Z0-9_-]+$",
            message = "GitHub URL 형식이 올바르지 않습니다. 예: https://github.com/username"
    )
    private String githubUrl;

    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    private MultipartFile profileImage; // 프로필 사진 추가



}