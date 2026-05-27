package com.github.antorof1.flightreservationservice.user.dto;

public record UserResponse(
    Long id,
    String email,
    String name
) {
}
