package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByEmail(String email);
    public Optional<User> findByEmail(String email);
    Optional <User>findBySocialId(String socialId);


}