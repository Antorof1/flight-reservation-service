package com.github.antorof1.flightreservationservice.user.dto;

import com.github.antorof1.flightreservationservice.user.User;

public record UserResponse(
    Long id,
    String email,
    String name
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName()
        );
    }
}
