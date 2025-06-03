package com.gucci.user_service.follow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Valid
@Data
public class RemoveFollowerDtoRequest {
    @NotNull(message = "삭제할 팔로워의 ID는 필수 항목입니다.")
    private Long followerId;
}