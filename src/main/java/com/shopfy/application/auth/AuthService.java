package com.shopfy.application.auth;

import com.shopfy.api.v1.dto.AuthResponse;
import com.shopfy.api.v1.dto.LoginRequest;
import com.shopfy.api.v1.dto.RegisterRequest;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import com.shopfy.domain.user.UserRepository;
import com.shopfy.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered: " + request.email());
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        var token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationMs(), user);
    }

    public AuthResponse login(LoginRequest request) {
        // Delega validação de credenciais ao AuthenticationManager do Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow();

        var token = jwtService.generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationMs(), user);
    }
}
