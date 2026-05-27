package com.github.antorof1.flightreservationservice.flight.dto;

import com.github.antorof1.flightreservationservice.flight.Flight;

import java.time.OffsetDateTime;

public record FlightResponse(
    Long id,
    String flightNumber,
    String departureAirport,
    String arrivalAirport,
    OffsetDateTime departureTime,
    OffsetDateTime arrivalTime
) {
    public static FlightResponse fromEntity(Flight flight) {
        return new FlightResponse(
            flight.getId(),
            flight.getFlightNumber(),
            flight.getDepartureAirport(),
            flight.getArrivalAirport(),
            flight.getDepartureTime(),
            flight.getArrivalTime()
        );
    }
}
