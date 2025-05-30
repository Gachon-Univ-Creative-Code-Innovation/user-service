package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.User;
import com.gucci.user_service.user.dto.MainUserInfoDto;
import com.gucci.user_service.user.dto.UserInfoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByEmail(String email);
    public Optional<User> findByEmail(String email);
    Optional <User>findBySocialId(String socialId);
    Boolean existsByNickname(String nickname);
    @Query("SELECT new com.gucci.user_service.user.dto.UserInfoDto(u.email, u.name, u.nickname, u.profileUrl, u.githubUrl) " +
            "FROM User u WHERE u.userId = :userId")
    UserInfoDto findUserInfoById(@Param("userId") Long userId);

    @Query("SELECT new com.gucci.user_service.user.dto.MainUserInfoDto(u.nickname, u.profileUrl, " +
           "(SELECT COUNT(f) FROM Follow f WHERE f.followee.userId = :userId), " +
           "(SELECT COUNT(f) FROM Follow f WHERE f.follower.userId = :userId)) " +
           "FROM User u WHERE u.userId = :userId")
    MainUserInfoDto findMainUserInfoById(@Param("userId") Long userId);

    @Query(""" 
           SELECT u.userId, u.nickname FROM User u
           WHERE u.userId IN :targetIds
    """)
    List<Object[]> findUserIdAndNicknameByIdIn(@Param("targetIds") List<Long> targetIds);

    @Query(""" 
           SELECT u.userId, u.profileUrl FROM User u
           WHERE u.userId IN :targetIds
    """)
    List<Object[]> findUserIdAndProfileByIdIn(List<Long> targetIds);
}