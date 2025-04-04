package com.gucci.user_service.user.controller;

import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoRequest;
import com.gucci.user_service.user.dto.SignUpDtoResponse;
import com.gucci.user_service.user.dto.VerifyCheckRequest;
import com.gucci.user_service.user.dto.VerifySendRequest;
import com.gucci.user_service.user.service.EmailVerificationService;
import com.gucci.user_service.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final Environment environment;
    private final EmailVerificationService emailService;


    @GetMapping("/health-check")
//    public ApiResponse<String> status(){
    public ResponseEntity<Response<String>> status(){
        String log = "Working on port " + environment.getProperty("local.server.port");
        Response<String> response= new Response<>(200, "서버 상태 확인", log);
//        return ApiResponse.success(SuccessCode.OK, log);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Response<SignUpDtoResponse>> signUp(@Valid @RequestBody SignUpDtoRequest signUpDtoRequest){

       User user = userService.signUp(signUpDtoRequest);
       SignUpDtoResponse signUpDtoResponse = new SignUpDtoResponse(user.getUserId());
       Response<SignUpDtoResponse> response = new Response<>(201, user.getName()+"님 회원가입 완료", signUpDtoResponse);

       return ResponseEntity.status(response.getStatus()).body(response);

    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Response<Boolean>> checkEmail(@PathVariable String email) {

        boolean isDuplicated = userService.isEmailDuplicated(email);
        Response<Boolean> response = new Response<>(200, "이메일 중복 확인", isDuplicated);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verify/send")
    public ResponseEntity<Response<Null>> sendCode(@Valid @RequestBody VerifySendRequest verifySendRequest) {

        emailService.sendVerificationCode(verifySendRequest.getEmail());
        Response<Null> response = new Response<>(200, "인증 코드가 전송되었습니다.", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verify/check")
    public ResponseEntity<Response<String>> verifyCode(@Valid @RequestBody VerifyCheckRequest verifyCheckRequest) {
        boolean isValid = emailService.verifyCode(verifyCheckRequest.getEmail(), verifyCheckRequest.getCode());
        Response<String> response = new Response<>(200, "이메일 인증 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }



}
