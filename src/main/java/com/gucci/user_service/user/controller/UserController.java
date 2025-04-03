package com.gucci.user_service.user.controller;

import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoReq;
import com.gucci.user_service.user.dto.SignUpDtoRes;
import com.gucci.user_service.user.dto.VerifyCheckReq;
import com.gucci.user_service.user.dto.VerifySendReq;
import com.gucci.user_service.user.service.EmailVerificationService;
import com.gucci.user_service.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final Environment environment;
    private final EmailVerificationService emailService;


    @GetMapping("/health-check")
    public String status(){
        return "Working on port " + environment.getProperty("local.server.port");
    }

    @PostMapping("/signup")
    public ResponseEntity<Response<SignUpDtoRes>> signUp(@Valid @RequestBody SignUpDtoReq signUpDtoReq){

       User user = userService.signUp(signUpDtoReq);
       SignUpDtoRes signUpDtoRes = new SignUpDtoRes(user.getUser_id());
       Response<SignUpDtoRes> response = new Response<>(201, user.getName()+"님 회원가입 완료", signUpDtoRes);

       return ResponseEntity.status(response.getStatus()).body(response);

    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Response<Boolean>> checkEmail(@PathVariable String email) {
        if (email == null || email.isEmpty()) {
            Response<Boolean> response = new Response<>(400, "이메일이 제공되지 않았습니다.", false);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        if (!isValidEmail(email)) {
            Response<Boolean> response = new Response<>(400, "잘못된 이메일 형식입니다.", false);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        boolean isDuplicated = userService.isEmailDuplicated(email);
        Response<Boolean> response = new Response<>(200, "이메일 중복 확인", isDuplicated);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verify/send")
    public ResponseEntity<Response<Null>> sendCode(@Valid @RequestBody VerifySendReq verifySendReq) {

        emailService.sendVerificationCode(verifySendReq.getEmail());
        Response<Null> response = new Response<>(200, "인증 코드가 전송되었습니다.", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verify/check")
    public ResponseEntity<Response<String>> verifyCode(@Valid @RequestBody VerifyCheckReq verifyCheckReq) {
        boolean isValid = emailService.verifyCode(verifyCheckReq.getEmail(), verifyCheckReq.getCode());
        Response<String> response = new Response<>(200, "이메일 인증 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

}
