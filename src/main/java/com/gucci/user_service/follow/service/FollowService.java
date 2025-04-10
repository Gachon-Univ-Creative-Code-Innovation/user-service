package com.gucci.user_service.follow.service;

import java.util.List;

public interface FollowService {

    void follow(Long followerId, Long followeeId);

    void unfollow(Long followerId, Long followeeId);

    boolean isFollowing(Long followerId, Long followeeId);

    List<Long> getFollowers(Long userId);

    List<Long> getFollowees(Long userId);
}
