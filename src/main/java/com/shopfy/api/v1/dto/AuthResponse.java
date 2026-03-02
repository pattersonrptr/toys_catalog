package com.shopfy.api.v1.dto;

import com.shopfy.domain.user.User;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresIn, User user) {
        return new AuthResponse(
                token,
                "Bearer",
                expiresIn,
                UserResponse.from(user)
        );
    }
}
