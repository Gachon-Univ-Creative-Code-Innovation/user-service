package com.gucci.user_service.user.config.error;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<EnumValid, String> {

    private Enum<?>[] enumConstants;

    @Override
    public void initialize(EnumValid annotation) {
        enumConstants = annotation.enumClass().getEnumConstants();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return Arrays.stream(enumConstants)
                .anyMatch(e -> e.name().equals(value));
    }
}