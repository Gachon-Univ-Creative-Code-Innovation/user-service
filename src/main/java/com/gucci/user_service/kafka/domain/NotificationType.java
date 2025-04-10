package com.gucci.user_service.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    FOLLOW("FOLLOW","님이 당신을 팔로우 했어요");

    private final String type;
    private final String message;
}