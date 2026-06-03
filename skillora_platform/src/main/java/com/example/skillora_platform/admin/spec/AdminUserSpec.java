package com.example.skillora_platform.admin.spec;

import org.springframework.data.jpa.domain.Specification;

import com.example.skillora_platform.user.entity.User;
import com.example.skillora_platform.user.entity.UserStatus;

public final class AdminUserSpec {

    private AdminUserSpec() {
    }

    public static Specification<User> statusEquals(UserStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null) {
                return null;
            }
            var join = root.join("roles");
            return cb.equal(join.get("name").as(String.class), roleName);
        };
    }

    public static Specification<User> searchByNameOrEmail(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}
