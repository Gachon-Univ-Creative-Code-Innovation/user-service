package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.Follow;
import com.gucci.user_service.user.domain.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    @Query("SELECT f.follower.userId FROM Follow f WHERE f.followee.userId = :userId")
    List<Long> findFollowerIdsByFolloweeId(@Param("userId") Long userId);

    @Query("SELECT f.followee.userId FROM Follow f WHERE f.follower.userId = :userId")
    List<Long> findFolloweeIdsByFollowerId(@Param("userId") Long userId);
}