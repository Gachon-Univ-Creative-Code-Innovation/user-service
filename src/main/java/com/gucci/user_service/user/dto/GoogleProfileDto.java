package com.gucci.user_service.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)// 없는 필드는 무시
public class GoogleProfileDto {
    private String sub;
    private String email;
    private String picture;
    private String name;
}