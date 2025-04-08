package com.gucci.user_service.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDtoResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;

}
