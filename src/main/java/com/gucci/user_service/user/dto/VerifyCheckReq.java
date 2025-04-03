package com.gucci.user_service.user.dto;

import com.gucci.user_service.user.config.validation.SixDigitCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCheckReq {
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;

    @NotBlank(message = "인증코드는 필수 항목입니다.")
    @SixDigitCode(message = "인증코드는 6자리 숫자여야 합니다.")
    private String code;
}
