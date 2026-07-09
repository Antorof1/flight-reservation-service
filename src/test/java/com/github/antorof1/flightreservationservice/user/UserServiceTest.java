package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should find user by id")
    void getUserById_Success() {
        Long userId = 1L;
        User user = new User(
            "test@example.com",
            "Test User",
            "password123",
            UserRole.USER
        );
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(userId);

        assertThat(foundUser.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user id not found")
    void getUserById_NotFound_ThrowsException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with id: " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should find user by email")
    void getUserByEmail_Success() {
        String email = "test@example.com";
        User user = new User(
            email,
            "Test User",
            "password123",
            UserRole.USER
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail(email);

        assertThat(foundUser.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user email not found")
    void getUserByEmail_NotFound_ThrowsException() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
    }
}
