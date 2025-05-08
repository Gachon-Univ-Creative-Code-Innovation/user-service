package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.UserInfoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByEmail(String email);
    public Optional<User> findByEmail(String email);
    Optional <User>findBySocialId(String socialId);
    Boolean existsByNickname(String nickname);
    @Query("SELECT new com.gucci.user_service.user.dto.UserInfoDto(u.email, u.name, u.nickname, u.profileUrl, u.githubUrl) " +
            "FROM User u WHERE u.userId = :userId")
    UserInfoDto findUserInfoById(@Param("userId") Long userId);
}