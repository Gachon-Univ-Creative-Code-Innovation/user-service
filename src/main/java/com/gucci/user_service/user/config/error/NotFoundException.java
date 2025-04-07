package com.gucci.user_service.user.config.error;


public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}