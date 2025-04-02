package com.gucci.user_service.user.repository;

import com.gucci.user_service.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByEmail(String email);
}