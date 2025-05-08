package com.gucci.user_service.user.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignUpDtoRequestTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validSignUpDtoReq() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("test@example.com");
        dto.setName("John Doe");
        dto.setPassword("password123");
        dto.setNickname("johndoe");
        dto.setGithubUsername("johndoe");
        dto.setRole("USER");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    void invalidEmailFormat() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("invalid-email");
        dto.setName("John Doe");
        dto.setPassword("password123");
        dto.setNickname("johndoe");
        dto.setGithubUsername("johndoe");
        dto.setRole("USER");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }


    @Test
    void blankName() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("test@example.com");
        dto.setName(""); // blank
        dto.setPassword("password123");
        dto.setNickname("johndoe");
        dto.setRole("USER");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);

        // 1. name 필드에 대한 violation만 필터링
        List<String> nameViolationMessages = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("name"))
                .map(ConstraintViolation::getMessage)
                .toList();

        // 2. 발생한 메시지 검증
        assertTrue(nameViolationMessages.contains("이름은 필수 항목입니다."));
        assertTrue(nameViolationMessages.contains("이름은 2~20자 사이여야 합니다."));
        assertEquals(2, nameViolationMessages.size());
    }

    @Test
    void shortPassword() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("test@example.com");
        dto.setName("John Doe");
        dto.setPassword("short");
        dto.setNickname("johndoe");
        dto.setGithubUsername("johndoe");
        dto.setRole("USER");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    void blankNickname() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("test@example.com");
        dto.setName("John Doe");
        dto.setPassword("password123");
        dto.setNickname("");
        dto.setRole("USER");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);

        List<String> nicknameViolationMessages = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("nickname"))
                .map(ConstraintViolation::getMessage)
                .toList();

        assertTrue(nicknameViolationMessages.contains("닉네임은 필수 항목입니다."));
        assertTrue(nicknameViolationMessages.contains("닉네임은 2~20자 사이여야 합니다."));
        assertEquals(2, nicknameViolationMessages.size());
    }

    @Test
    void invalidRole() {
        SignUpDtoRequest dto = new SignUpDtoRequest();
        dto.setEmail("test@example.com");
        dto.setName("John Doe");
        dto.setPassword("password123");
        dto.setNickname("johndoe");
        dto.setGithubUsername("johndoe");
        dto.setRole("INVALID_ROLE");

        Set<ConstraintViolation<SignUpDtoRequest>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }
}