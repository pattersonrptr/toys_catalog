package com.shopfy.application.auth;

import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import com.shopfy.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 64 hex chars = 256-bit key (required for HS256)
        ReflectionTestUtils.setField(jwtService, "secret",
                "1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86_400_000L);

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@shopfy.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("generates a non-blank token for a valid user")
    void generateToken_returnsNonBlankToken() {
        var token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extracts the correct username (email) from the token")
    void extractUsername_returnsEmail() {
        var token = jwtService.generateToken(user);
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("validates a freshly generated token as valid")
    void isTokenValid_trueForFreshToken() {
        var token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("rejects a token that belongs to a different user")
    void isTokenValid_falseForWrongUser() {
        var token = jwtService.generateToken(user);

        var otherUser = User.builder()
                .id(2L)
                .name("Other User")
                .email("other@shopfy.com")
                .password("encoded-password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("rejects an expired token")
    void isTokenValid_falseForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1L); // already expired
        var token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    @DisplayName("getExpirationMs returns the configured value")
    void getExpirationMs_returnsConfiguredValue() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(86_400_000L);
    }
}
