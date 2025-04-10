package com.gucci.user_service.user.service;

import com.gucci.user_service.user.config.error.NotFoundException;
import com.gucci.user_service.user.domain.Follow;
import com.gucci.user_service.user.domain.FollowId;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.repository.FollowRepository;
import com.gucci.user_service.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            log.warn("User {} cannot follow themselves", followerId);
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        if (isFollowing(followerId, followeeId)) {
            log.warn("User {} is already following user {}", followerId, followeeId);
            throw new IllegalArgumentException(
                    String.format("User %s is already following user %s", followerId, followeeId)
            );
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new NotFoundException("Follower not found"));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new NotFoundException("Followee not found"));

        Follow follow = Follow.builder()
                .followId(new FollowId(followerId, followeeId))
                .follower(follower)
                .followee(followee)
                .build();
        followRepository.save(follow);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        FollowId followId = new FollowId(followerId, followeeId);
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new NotFoundException("Follow relationship not found"));

        followRepository.delete(follow);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followeeId) {
        FollowId followId = new FollowId(followerId, followeeId);
        return followRepository.existsById(followId);
    }

    @Override
    public List<Long> getFollowers(Long userId) {
        return followRepository.findFollowerIdsByFolloweeId(userId);
    }

    @Override
    public List<Long> getFollowees(Long userId) {
        return followRepository.findFolloweeIdsByFollowerId(userId);
    }
}