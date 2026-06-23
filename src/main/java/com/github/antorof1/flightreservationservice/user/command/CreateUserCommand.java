package com.github.antorof1.flightreservationservice.user.command;

public record CreateUserCommand(
    String email,
    String name,
    String password
) {
}
