package com.shopfy.application.auth;

import com.shopfy.api.v1.dto.LoginRequest;
import com.shopfy.api.v1.dto.RegisterRequest;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import com.shopfy.domain.user.UserRepository;
import com.shopfy.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: saves a new CUSTOMER and returns a JWT response")
    void register_savesUserAndReturnsToken() {
        var request = new RegisterRequest("Alice", "alice@shopfy.com", "password123");

        given(userRepository.existsByEmail("alice@shopfy.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
        given(jwtService.generateToken(any(User.class))).willReturn("mocked.jwt.token");
        given(jwtService.getExpirationMs()).willReturn(86_400_000L);

        var response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().email()).isEqualTo("alice@shopfy.com");

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    @DisplayName("register: throws BusinessException when email is already taken")
    void register_throwsWhenEmailTaken() {
        given(userRepository.existsByEmail("taken@shopfy.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Bob", "taken@shopfy.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: returns JWT response for valid credentials")
    void login_returnsTokenForValidCredentials() {
        var request = new LoginRequest("admin@shopfy.com", "Admin@123");

        var storedUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@shopfy.com")
                .password("encoded-password")
                .role(Role.ADMIN)
                .active(true)
                .build();

        given(userRepository.findByEmail("admin@shopfy.com")).willReturn(Optional.of(storedUser));
        given(jwtService.generateToken(storedUser)).willReturn("mocked.jwt.token");
        given(jwtService.getExpirationMs()).willReturn(86_400_000L);

        var response = authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("admin@shopfy.com", "Admin@123"));

        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.user().role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("login: propagates BadCredentialsException on wrong password")
    void login_throwsOnBadCredentials() {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("admin@shopfy.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
