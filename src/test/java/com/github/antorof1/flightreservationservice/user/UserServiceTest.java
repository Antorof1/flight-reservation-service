package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.user.command.CreateUserCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully create a new user")
    void createUser_Success() {
        User user = new User(
            "test@example.com",
            "Test User",
            "password123",
            UserRole.USER
        );
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        CreateUserCommand command = new CreateUserCommand(
            user.getEmail(),
            user.getName(),
            user.getPassword()
        );

        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");

        User createdUser = userService.createUser(command);

        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getName()).isEqualTo("Test User");
        verify(userRepository).existsByEmail(user.getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getPassword()).isEqualTo("hashed_password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email already exists")
    void createUser_EmailExists_ThrowsException() {
        User user = new User(
            "existing@example.com",
            "Existing User",
            "password123",
            UserRole.USER
        );
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        CreateUserCommand command = new CreateUserCommand(
            user.getEmail(),
            user.getName(),
            user.getPassword()
        );

        assertThatThrownBy(() -> userService.createUser(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email is already in use");

        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

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
