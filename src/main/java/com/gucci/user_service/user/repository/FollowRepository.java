package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.Follow;
import com.gucci.user_service.user.domain.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
}