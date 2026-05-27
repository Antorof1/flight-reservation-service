package com.github.antorof1.flightreservationservice.flight.dto;

import java.time.OffsetDateTime;

public record FlightResponse(
    Long id,
    String flightNumber,
    String departureAirport,
    String arrivalAirport,
    OffsetDateTime departureTime,
    OffsetDateTime arrivalTime
) {
}
