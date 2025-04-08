    package com.gucci.user_service.user.config.error;

    import com.gucci.user_service.user.config.ErrorResponse;
    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationServiceException;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
    import org.springframework.web.servlet.resource.NoResourceFoundException;

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

        // 🔹 NotFoundException (사용자를 찾을 수 없는 경우)
        @ExceptionHandler(NotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(404, e.getMessage(), null),
                    HttpStatus.NOT_FOUND
            );
        }

        // 🔹 NoResourceFoundException (리소스를 찾을 수 없는 경우)
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(404, "요청한 리소스를 찾을 수 없습니다.", null),
                    HttpStatus.NOT_FOUND
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
        // 🔹 TokenMissingException (토큰이 없는 경우)
        @ExceptionHandler(TokenMissingException.class)
        public ResponseEntity<ErrorResponse> handleTokenMissing(TokenMissingException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(401, e.getMessage(), null),
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 🔹 TokenExpiredException (토큰이 만료된 경우)
        @ExceptionHandler(TokenExpiredException.class)
        public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(401, e.getMessage(), null),
                    HttpStatus.UNAUTHORIZED
            );
        }
        // 🔹 ForbiddenException (접근 권한이 없는 경우)
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(403, e.getMessage(), null),
                    HttpStatus.FORBIDDEN
            );
        }

        // 🔹 DataIntegrityViolationException (데이터 무결성 위반)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(400, "데이터 무결성 위반: " + e.getRootCause().getMessage(), null),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 🔹 AuthenticationServiceException (인증 서비스 예외)
        @ExceptionHandler(AuthenticationServiceException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationServiceException(AuthenticationServiceException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(401, e.getMessage(), null),
                    HttpStatus.UNAUTHORIZED
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