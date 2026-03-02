package com.shopfy.api.v1.dto;

import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
