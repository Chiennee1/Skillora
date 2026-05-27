package com.example.skillora_platform.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
