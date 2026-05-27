package com.example.skillora_platform.user.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.skillora_platform.config.JwtProperties;
import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim(ROLES_CLAIM, roleNames(user))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenExpiresInSeconds() {
        return jwtProperties.accessTokenTtl().toSeconds();
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
