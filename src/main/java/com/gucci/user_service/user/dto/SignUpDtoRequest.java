package com.gucci.user_service.user.dto;

import com.gucci.user_service.user.config.error.EnumValid;
import com.gucci.user_service.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SignUpDtoRequest {


    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;

    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 항목입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    private String githubUsername;


    @NotNull(message = "역할(Role)은 필수 항목입니다.")
    @EnumValid(enumClass = Role.class, message = "역할은 ADMIN, USER, HEADHUNTER 중 하나여야 합니다.")
    private String role;

    private MultipartFile profileImage; // 프로필 사진 추가

}