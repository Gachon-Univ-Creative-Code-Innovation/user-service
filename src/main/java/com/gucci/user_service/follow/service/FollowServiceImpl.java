package com.gucci.user_service.follow.service;

import com.gucci.user_service.kafka.domain.NotificationType;
import com.gucci.user_service.kafka.dto.NotificationKafkaRequest;
import com.gucci.user_service.kafka.producer.NotificationProducer;
import com.gucci.user_service.user.config.error.NotFoundException;
import com.gucci.user_service.follow.domain.Follow;
import com.gucci.user_service.follow.domain.FollowId;
import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.follow.repository.FollowRepository;
import com.gucci.user_service.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

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

        //followeeId 가 알림을 받아야함
        notificationProducer.sendNotification(NotificationKafkaRequest.builder()
                .receiverId(followeeId)
                .senderId(followerId)
                .type(NotificationType.FOLLOW.getType())
                .content(follower.getNickname() + NotificationType.FOLLOW.getMessage())
                .targetUrl("/user/" + followerId) // 팔로워의 블로그 주소(임시 경로)
                .build());
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