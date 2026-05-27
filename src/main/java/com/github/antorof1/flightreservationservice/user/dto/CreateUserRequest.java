package com.github.antorof1.flightreservationservice.user.dto;

import com.github.antorof1.flightreservationservice.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Name is required")
    String name
) {
    public User toEntity() {
        User user = new User();

        user.setEmail(email);
        user.setName(name);

        return user;
    }
}
