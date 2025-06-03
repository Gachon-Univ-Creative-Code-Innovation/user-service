package com.gucci.user_service.follow.controller;

import com.gucci.user_service.follow.dto.RemoveFollowerDtoRequest;
import com.gucci.user_service.user.config.Response;
import com.gucci.user_service.follow.dto.FollowDtoRequest;
import com.gucci.user_service.follow.service.FollowService;
import com.gucci.user_service.user.config.security.auth.JwtTokenProvider;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user-service/follow")
@Slf4j
public class FollowController {
    private final FollowService followService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<Response<Null>> follow(@RequestHeader("Authorization") String token, @RequestBody FollowDtoRequest followDtoRequest) {
        String jwt = getJwtToken(token);
        Long followerId = jwtTokenProvider.getUserIdFromToken(jwt);
        followService.follow(followerId, followDtoRequest.getFolloweeId());

        Response<Null> response = new Response<>(200, "팔로우 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


    @DeleteMapping
    public ResponseEntity<Response<Null>> unfollow(@RequestHeader("Authorization") String token, @RequestBody FollowDtoRequest followDtoRequest) {
        String jwt = getJwtToken(token);
        Long followerId = jwtTokenProvider.getUserIdFromToken(jwt);
        followService.unfollow(followerId, followDtoRequest.getFolloweeId());

        Response<Null> response = new Response<>(200, "언팔로우 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);

    }


    @GetMapping("/followers")
    public ResponseEntity<Response<List<Long>>> getFollowers(@RequestHeader("Authorization") String token) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        List<Long> followers = followService.getFollowers(userId);
        Response<List<Long>>response = new Response<>(200, "팔로워 목록 조회 성공",followers);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/followees")
    public ResponseEntity<Response<List<Long>>> getFollowees(@RequestHeader("Authorization") String token) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        List<Long> followees = followService.getFollowees(userId);
        Response<List<Long>> response = new Response<>(200, "팔로잉 목록 조회 성공", followees);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<Response<Null>> removeFollower(@RequestHeader("Authorization") String token, @RequestBody RemoveFollowerDtoRequest removeFollowerDtoRequest) {
        String jwt = getJwtToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
        followService.removeFollower(userId, removeFollowerDtoRequest.getFollowerId());

        Response<Null> response = new Response<>(200, "팔로워 삭제 성공", null);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    private String getJwtToken(String token) {
        return token.replace("Bearer", "").trim();
    }
}