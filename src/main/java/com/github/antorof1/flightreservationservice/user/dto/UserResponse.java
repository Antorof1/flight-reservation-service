package com.github.antorof1.flightreservationservice.user.dto;

import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing user details")
public record UserResponse(
    @Schema(description = "Unique identifier of the user")
    Long id,

    @Schema(description = "The email address of the user", example = "john.doe@example.com")
    String email,

    @Schema(description = "The full name of the user", example = "John Doe")
    String name,

    @Schema(description = "The role of the user", example = "USER")
    UserRole role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole()
        );
    }
}
