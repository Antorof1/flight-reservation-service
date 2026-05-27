package com.github.antorof1.flightreservationservice.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateFlightRequest(
    @NotBlank
    String flightNumber,

    @NotBlank
    String departureAirport,

    @NotBlank
    String arrivalAirport,

    @NotNull
    OffsetDateTime departureTime,

    @NotNull
    OffsetDateTime arrivalTime
) {
}
