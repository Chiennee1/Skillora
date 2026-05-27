package com.example.skillora_platform.user.config;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.skillora_platform.user.entity.Role;
import com.example.skillora_platform.user.entity.RoleName;
import com.example.skillora_platform.user.repository.RoleRepository;

@Configuration
public class RoleBootstrap {

    @Bean
    CommandLineRunner ensureDefaultRoles(RoleRepository roleRepository) {
        return args -> {
            Map<RoleName, String> descriptions = Map.of(
                    RoleName.ADMIN, "System administrator with full platform permissions",
                    RoleName.INSTRUCTOR, "Instructor who can create and manage courses",
                    RoleName.STUDENT, "Student who can enroll and learn courses"
            );
            descriptions.forEach((roleName, description) -> {
                if (!roleRepository.existsByName(roleName)) {
                    roleRepository.save(Role.builder()
                            .name(roleName)
                            .description(description)
                            .build());
                }
            });
        };
    }
}
