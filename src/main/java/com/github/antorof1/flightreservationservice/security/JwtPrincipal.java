package com.github.antorof1.flightreservationservice.security;

import com.github.antorof1.flightreservationservice.user.UserRole;

public record JwtPrincipal(Long id, UserRole role) {
}
