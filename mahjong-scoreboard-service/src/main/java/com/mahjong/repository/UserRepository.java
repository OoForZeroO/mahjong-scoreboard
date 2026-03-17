package com.mahjong.repository;

import com.mahjong.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhone(String phone);
    boolean existsByPhone(String phone);

    Optional<User> findByUsername(String username);
}