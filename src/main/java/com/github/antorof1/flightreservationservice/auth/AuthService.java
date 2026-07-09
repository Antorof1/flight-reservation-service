package com.github.antorof1.flightreservationservice.auth;

import com.github.antorof1.flightreservationservice.auth.dto.AuthResponse;
import com.github.antorof1.flightreservationservice.exception.DuplicateResourceException;
import com.github.antorof1.flightreservationservice.security.JwtUtils;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRepository;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(String email, String name, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(
            email,
            name,
            encodedPassword,
            UserRole.USER
        );

        user = userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId().toString(), Map.of("role", user.getRole()));

        return new AuthResponse(
            token,
            email,
            name
        );
    }

    public AuthResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        String authenticatedEmail = authentication.getName();

        User user = userRepository.findByEmail(authenticatedEmail)
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        String token = jwtUtils.generateToken(user.getId().toString(), Map.of("role", user.getRole()));

        return new AuthResponse(
            token,
            user.getEmail(),
            user.getName()
        );
    }
}
