package com.github.antorof1.flightreservationservice.flight.dto;

import com.github.antorof1.flightreservationservice.flight.Flight;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Response object containing flight details")
public record FlightResponse(
    @Schema(description = "Unique identifier of the flight")
    Long id,

    @Schema(description = "The flight number", example = "AA123")
    String flightNumber,

    @Schema(description = "The departure airport code", example = "JFK")
    String departureAirport,

    @Schema(description = "The arrival airport code", example = "LAX")
    String arrivalAirport,

    @Schema(description = "The scheduled departure time")
    OffsetDateTime departureTime,

    @Schema(description = "The scheduled arrival time")
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
