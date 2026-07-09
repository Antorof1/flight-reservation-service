package com.github.antorof1.flightreservationservice.auth;

import com.github.antorof1.flightreservationservice.auth.dto.AuthResponse;
import com.github.antorof1.flightreservationservice.exception.DuplicateResourceException;
import com.github.antorof1.flightreservationservice.security.JwtUtils;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRepository;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    @DisplayName("register() should encode the password, persist a USER-role account and return a token")
    void register_Success() {
        String email = "john.doe@example.com";
        String name = "John Doe";
        String rawPassword = "password123";
        String encodedPassword = "encoded-password";
        String token = "generated-token";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtils.generateToken(eq("1"), any())).thenReturn(token);

        AuthResponse response = authService.register(email, name, rawPassword);

        assertThat(response.token()).isEqualTo(token);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.name()).isEqualTo(name);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getName()).isEqualTo(name);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);

        verify(jwtUtils).generateToken("1", Map.of("role", UserRole.USER));
    }

    @Test
    @DisplayName("register() should throw DuplicateResourceException when the email is already in use")
    void register_DuplicateEmail_ThrowsException() {
        String email = "john.doe@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(email, "John Doe", "password123"))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining(email);

        verify(userRepository, never()).save(any());
        verify(jwtUtils, never()).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("login() should authenticate the credentials and return a token for the matching user")
    void login_Success() {
        String email = "john.doe@example.com";
        String password = "password123";
        String token = "generated-token";

        User user = new User(email, "John Doe", "encoded-password", UserRole.ADMIN);
        user.setId(1L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(eq("1"), any())).thenReturn(token);

        AuthResponse response = authService.login(email, password);

        assertThat(response.token()).isEqualTo(token);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.name()).isEqualTo("John Doe");

        verify(jwtUtils).generateToken("1", Map.of("role", UserRole.ADMIN));
    }

    @Test
    @DisplayName("login() should propagate the authentication failure without generating a token")
    void login_InvalidCredentials_PropagatesException() {
        String email = "john.doe@example.com";
        String password = "wrong-password";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(email, password))
            .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtils, never()).generateToken(anyString(), any());
    }

    @Test
    @DisplayName("login() should throw IllegalStateException when the authenticated user cannot be reloaded")
    void login_AuthenticatedUserMissing_ThrowsException() {
        String email = "ghost@example.com";
        String password = "password123";

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, password))
            .isInstanceOf(IllegalStateException.class);

        verify(jwtUtils, never()).generateToken(anyString(), any());
    }
}
