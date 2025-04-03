package com.gucci.user_service.user.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SixDigitCodeValidator implements ConstraintValidator<SixDigitCode, String> {
    private String message;

    @Override
    public void initialize(SixDigitCode constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String code, ConstraintValidatorContext context) {
        if (code == null || !code.matches("\\d{6}")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
