package com.gucci.user_service.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Valid
public class RefreshTokenDtoRequest {

    @NotBlank(message = "RefreshToken은 필수 값입니다.")
    private String refreshToken;
}
