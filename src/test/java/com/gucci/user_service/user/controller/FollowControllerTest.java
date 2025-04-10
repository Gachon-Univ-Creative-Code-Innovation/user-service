package com.gucci.user_service.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gucci.user_service.follow.controller.FollowController;
import com.gucci.user_service.user.config.error.GlobalExceptionHandler;
import com.gucci.user_service.user.config.security.auth.JwtTokenProvider;
import com.gucci.user_service.follow.dto.FollowDtoRequest;
import com.gucci.user_service.follow.service.FollowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({FollowControllerTest.MockServiceConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test") // <- 추가
class FollowControllerTest {

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public FollowService followService() {
            return Mockito.mock(FollowService.class);
        }

        @Bean
        public JwtTokenProvider jwtTokenProvider() {
            return Mockito.mock(JwtTokenProvider.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Follow API (/api/user-service/follow)")
    class FollowTests {

        @Test
        @DisplayName("Success: Follow a user")
        void followUser() throws Exception {
            FollowDtoRequest request = new FollowDtoRequest();
            request.setFolloweeId(2L);

            Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(1L);

            ResultActions result = mockMvc.perform(post("/api/user-service/follow")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("팔로우 성공"));
        }

        @Test
        @DisplayName("Failure: Follow self")
        void followSelf() throws Exception {
            FollowDtoRequest request = new FollowDtoRequest();
            request.setFolloweeId(1L);

            Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(1L);
            Mockito.doThrow(new IllegalArgumentException("User cannot follow themselves"))
                    .when(followService).follow(1L, 1L);

            ResultActions result = mockMvc.perform(post("/api/user-service/follow")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("User cannot follow themselves"));
        }

        @Test
        @DisplayName("Success: Unfollow a user")
        void unfollowUser() throws Exception {
            FollowDtoRequest request = new FollowDtoRequest();
            request.setFolloweeId(2L);

            Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(1L);

            ResultActions result = mockMvc.perform(delete("/api/user-service/follow")
                    .header("Authorization", "Bearer token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("언팔로우 성공"));
        }

        @Test
        @DisplayName("Success: Get followers")
        void getFollowers() throws Exception {
            Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(1L);
            Mockito.when(followService.getFollowers(1L)).thenReturn(List.of(2L, 3L));

            ResultActions result = mockMvc.perform(get("/api/user-service/follow/followers")
                    .header("Authorization", "Bearer token"));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("팔로워 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("Success: Get followees")
        void getFollowees() throws Exception {
            Mockito.when(jwtTokenProvider.getUserIdFromToken(Mockito.anyString())).thenReturn(1L);
            Mockito.when(followService.getFollowees(1L)).thenReturn(List.of(2L, 3L));

            ResultActions result = mockMvc.perform(get("/api/user-service/follow/followees")
                    .header("Authorization", "Bearer token"));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("팔로잉 목록 조회 성공"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}