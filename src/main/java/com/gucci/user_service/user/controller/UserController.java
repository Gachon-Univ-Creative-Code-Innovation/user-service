package com.gucci.user_service.user.controller;

import com.gucci.user_service.follow.dto.FollowDtoRequest;
import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.user.config.error.TokenMissingException;
import com.gucci.user_service.user.config.security.auth.JwtTokenProvider;
import com.gucci.user_service.user.domain.SocialType;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.*;
import com.gucci.user_service.user.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user-service")
public class UserController {
    private final UserService userService;
    private final Environment environment;
    private final EmailVerificationService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final GoogleService googleService;
    private final KakaoService kakaoService;

    @GetMapping("/health-check")
    public ResponseEntity<Response<String>> status(){
        String log = "Working on port " + environment.getProperty("local.server.port");
        Response<String> response= new Response<>(200, "서버 상태 확인", log);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping(value = "/signup", consumes = {"multipart/form-data"})
    public ResponseEntity<Response<SignUpDtoResponse>> signUp(@Valid @ModelAttribute  SignUpDtoRequest signUpDtoRequest){

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

    @GetMapping("/check-nickname/{nickname}")
    public ResponseEntity<Response<Boolean>> checkNickname(@PathVariable String nickname) {
        boolean isDuplicated = userService.isNicknameDuplicated(nickname);
        Response<Boolean> response = new Response<>(200, "닉네임 중복 확인", isDuplicated);

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


    @PostMapping("/signin")
    public ResponseEntity<?>login(@RequestBody @Valid LoginDtoRequest loginDTORequest){
        User user = userService.login(loginDTORequest);
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getUserId(),
                user.getRole().toString(),
                user.getNickname()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getEmail(),
                user.getUserId(),
                user.getRole().toString(),
                user.getNickname()
        );

        tokenService.saveRefreshToken(user.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration());


        Response<LoginDtoResponse> response = new Response<>(201, "로그인 성공", new LoginDtoResponse(accessToken, refreshToken, false));

        return ResponseEntity.status(response.getStatus()).body(response);

    }


    @PostMapping("/google/login")
    public ResponseEntity<?>googleLogin(@RequestBody RedirectDto redirectDto){
        //accessToken 발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

        boolean isNewUser = false;

        //사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getProfile(accessTokenDto.getAccess_token());

        //회원가입이 되어 있지 않다면 회원가입 시키기
        User originalMember = userService.getUserBySocialId(googleProfileDto.getSub());
        if(originalMember == null){
            isNewUser = true;
            originalMember = userService.createOauth(
                    googleProfileDto.getSub(),
                    googleProfileDto.getEmail(),
                    googleProfileDto.getName(),
                    SocialType.GOOGLE, googleProfileDto.getPicture()
            );
        }

        //회원가입 되어 있는 회원이라면 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                originalMember.getEmail(),
                originalMember.getUserId(),
                originalMember.getRole().toString(),
                originalMember.getNickname()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                originalMember.getEmail(),
                originalMember.getUserId(),
                originalMember.getRole().toString(),
                originalMember.getNickname()
        );
        tokenService.saveRefreshToken(originalMember.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration());



        Response<LoginDtoResponse> response = new Response<>(
                200, "로그인 성공", new LoginDtoResponse(accessToken, refreshToken, isNewUser)
        );

        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @PostMapping("/kakao/login")
    public ResponseEntity<?>kakaoLogin(@RequestBody RedirectDto redirectDto){
        boolean isNewUser = false;
        //accessToken 발급
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(redirectDto.getCode());
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());
        User originalMember = userService.getUserBySocialId(kakaoProfileDto.getId());
        if(originalMember == null){
            isNewUser = true;
            originalMember =
                    userService
                            .createOauth(
                                    kakaoProfileDto.getId(),
                                    kakaoProfileDto.getKakao_account().getEmail(),
                                    kakaoProfileDto.getKakao_account().getProfile().getNickname(),
                                    SocialType.KAKAO,
                                    kakaoProfileDto.getKakao_account().getProfile().getProfile_image_url()

                            );
        }

        //회원가입 되어 있는 회원이라면 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                originalMember.getEmail(),
                originalMember.getUserId(),
                originalMember.getRole().toString(),
                originalMember.getNickname()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                originalMember.getEmail(),
                originalMember.getUserId(),
                originalMember.getRole().toString(),
                originalMember.getNickname()
        );
        tokenService.saveRefreshToken(originalMember.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration());

        Response<LoginDtoResponse> response = new Response<>(
                200, "로그인 성공", new LoginDtoResponse(accessToken, refreshToken, isNewUser)
        );

        return ResponseEntity.status(response.getStatus()).body(response);



    }
    @PostMapping("/refresh-token")
    public ResponseEntity<Response<AccessTokenDtoResponse>> refreshAccessToken(
            @RequestBody RefreshTokenDtoRequest refreshTokenDtoRequest) {
        String refreshToken = refreshTokenDtoRequest.getRefreshToken();
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        if(tokenService.isValidRefreshToken(refreshToken, userId)){

            String accessToken = jwtTokenProvider.createAccessToken(
                    jwtTokenProvider.getEmailFromToken(refreshToken),
                    userId,
                    jwtTokenProvider.getRoleFromToken(refreshToken),
                    jwtTokenProvider.getNicknameFromToken(refreshToken)
            );


            Response<AccessTokenDtoResponse> response = new Response<>(
                    200,
                    "Access Token 재발급 성공",
                    new AccessTokenDtoResponse(accessToken));
            return ResponseEntity.status(response.getStatus()).body(response);
        }else {
            throw new TokenMissingException("RefreshToken이 유효하지 않습니다.");
        }
    }


    @GetMapping("/user")
    public ResponseEntity<Response<UserInfoDto>> getUserById(@RequestHeader("Authorization") String token) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        UserInfoDto userInfo = userService.getUserInfoById(userId);

        Response<UserInfoDto> response = new Response<>(200, "회원 정보 조회 성공", userInfo);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PatchMapping(value = "/user", consumes = {"multipart/form-data"})
    public ResponseEntity<Response<String>> updateUser(
            @RequestHeader("Authorization") String token,
            @ModelAttribute @Valid UpdateUserDtoRequest updateUserDtoRequest) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

        userService.updateUser(userId, updateUserDtoRequest);

        Response<String> response = new Response<>(200, "회원 정보 수정 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @GetMapping("/user/main")
    public ResponseEntity<Response<MainUserInfoDto>> getMainUserInfo(@RequestHeader("Authorization") String token) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

        MainUserInfoDto mainUserInfo = userService.getMainUserInfo(userId);

        Response<MainUserInfoDto> response = new Response<>(200, "메인 페이지 정보 조회 성공", mainUserInfo);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/test")
    public String test(){
        return "jenkins 연동 성공2";
    }

    private String getJwtToken(String token) {
        return token.replace("Bearer", "").trim();
    }


}
