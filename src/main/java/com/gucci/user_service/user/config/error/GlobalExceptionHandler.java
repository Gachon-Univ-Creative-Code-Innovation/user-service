package com.gucci.user_service.user.config.error;

import com.gucci.user_service.user.config.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 유효성 검사 실패 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(
                new ErrorResponse(400, "입력값이 올바르지 않습니다.", errorMessages),
                HttpStatus.BAD_REQUEST
        );
    }

    // 🔹 IllegalArgumentException (비즈니스 로직 중 예외 발생 시)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return new ResponseEntity<>(
                new ErrorResponse(400, e.getMessage(), null),
                HttpStatus.BAD_REQUEST
        );
    }

    // 🔹 사용자 정의 예외 (선택적으로 추가)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException e) {
        return new ResponseEntity<>(
                new ErrorResponse(e.getStatus(), e.getMessage(), null),
                HttpStatus.valueOf(e.getStatus())
        );
    }

    // 🔹 기타 예상 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return new ResponseEntity<>(
                new ErrorResponse(500, "서버 오류가 발생했습니다.", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}