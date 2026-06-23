package com.github.antorof1.flightreservationservice.user.dto;

import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import com.github.antorof1.flightreservationservice.user.command.CreateUserCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating a new user")
public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "The email address of the user", example = "john.doe@example.com")
    String email,

    @NotBlank(message = "Name is required")
    @Schema(description = "The full name of the user", example = "John Doe")
    String name,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "The plain text password of the user", example = "password123")
    String password
) {
    public User toEntity() {
        User user = new User();

        user.setEmail(email);
        user.setName(name);
        user.setRole(UserRole.USER);

        return user;
    }

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(
            email,
            name,
            password
        );
    }
}
