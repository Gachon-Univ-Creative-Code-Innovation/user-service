package com.gucci.user_service.user.service;

import com.gucci.user_service.user.config.error.NotFoundException;
import com.gucci.user_service.user.domain.Follow;
import com.gucci.user_service.user.domain.FollowId;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.repository.FollowRepository;
import com.gucci.user_service.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void follow_Success() {
        Long followerId = 1L;
        Long followeeId = 2L;
        User follower = new User();
        User followee = new User();

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followeeId)).thenReturn(Optional.of(followee));
        when(followRepository.existsById(any(FollowId.class))).thenReturn(false);

        followService.follow(followerId, followeeId);

        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    public void follow_UserCannotFollowThemselves() {
        Long userId = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            followService.follow(userId, userId);
        });

        assertEquals("User cannot follow themselves", exception.getMessage());
    }

    @Test
    public void follow_AlreadyFollowing() {
        Long followerId = 1L;
        Long followeeId = 2L;

        when(followRepository.existsById(any(FollowId.class))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            followService.follow(followerId, followeeId);
        });

        assertEquals("User 1 is already following user 2", exception.getMessage());
    }

    @Test
    public void unfollow_Success() {
        Long followerId = 1L;
        Long followeeId = 2L;
        Follow follow = new Follow();

        when(followRepository.findById(any(FollowId.class))).thenReturn(Optional.of(follow));

        followService.unfollow(followerId, followeeId);

        verify(followRepository, times(1)).delete(follow);
    }

    @Test
    public void unfollow_FollowRelationshipNotFound() {
        Long followerId = 1L;
        Long followeeId = 2L;

        when(followRepository.findById(any(FollowId.class))).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            followService.unfollow(followerId, followeeId);
        });

        assertEquals("Follow relationship not found", exception.getMessage());
    }

    @Test
    public void isFollowing_ReturnsTrue() {
        Long followerId = 1L;
        Long followeeId = 2L;

        when(followRepository.existsById(any(FollowId.class))).thenReturn(true);

        assertTrue(followService.isFollowing(followerId, followeeId));
    }

    @Test
    public void isFollowing_ReturnsFalse() {
        Long followerId = 1L;
        Long followeeId = 2L;

        when(followRepository.existsById(any(FollowId.class))).thenReturn(false);

        assertFalse(followService.isFollowing(followerId, followeeId));
    }
}