package com.github.antorof1.flightreservationservice.auth.dto;

public record AuthResponse(
    String token,
    String email,
    String name
) {
}
