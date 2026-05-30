package com.github.antorof1.flightreservationservice.flight.dto;

import com.github.antorof1.flightreservationservice.flight.Flight;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "Request object for creating a new flight")
public record CreateFlightRequest(
    @NotBlank
    @Schema(description = "The flight number", example = "AA123")
    String flightNumber,

    @NotBlank
    @Schema(description = "The departure airport code", example = "JFK")
    String departureAirport,

    @NotBlank
    @Schema(description = "The arrival airport code", example = "LAX")
    String arrivalAirport,

    @NotNull
    @Schema(description = "The scheduled departure time")
    OffsetDateTime departureTime,

    @NotNull
    @Schema(description = "The scheduled arrival time")
    OffsetDateTime arrivalTime
) {
    public Flight toEntity() {
        Flight flight = new Flight();

        flight.setFlightNumber(flightNumber);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);

        return flight;
    }
}
