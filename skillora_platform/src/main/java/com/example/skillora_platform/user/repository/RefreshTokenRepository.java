package com.example.skillora_platform.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.user.entity.RefreshToken;
import com.example.skillora_platform.user.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);
}
