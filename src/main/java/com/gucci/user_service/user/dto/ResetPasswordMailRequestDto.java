package com.gucci.user_service.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPasswordMailRequestDto {
    private String email;
}
