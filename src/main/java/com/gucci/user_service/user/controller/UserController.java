package com.gucci.user_service.user.controller;

import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.SignUpDtoReq;
import com.gucci.user_service.user.dto.SignUpDtoRes;
import com.gucci.user_service.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.Builder;
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

}
