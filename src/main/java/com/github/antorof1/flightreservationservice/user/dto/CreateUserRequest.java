package com.github.antorof1.flightreservationservice.user.dto;

import com.github.antorof1.flightreservationservice.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object for creating a new user")
public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "The email address of the user", example = "john.doe@example.com")
    String email,

    @NotBlank(message = "Name is required")
    @Schema(description = "The full name of the user", example = "John Doe")
    String name
) {
    public User toEntity() {
        User user = new User();

        user.setEmail(email);
        user.setName(name);

        return user;
    }
}
