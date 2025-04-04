package com.gucci.user_service.user.config.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SixDigitCodeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SixDigitCode {
    String message() default "The code must be a 6-digit number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}