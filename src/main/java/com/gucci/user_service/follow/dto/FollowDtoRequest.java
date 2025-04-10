package com.gucci.user_service.follow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Valid
@Data
public class FollowDtoRequest {
    @NotNull(message = "팔로우 할 대상의 ID는 필수 항목입니다.")
    private Long followeeId;

}
