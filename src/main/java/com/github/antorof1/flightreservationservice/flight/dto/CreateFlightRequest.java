package com.github.antorof1.flightreservationservice.flight.dto;

import com.github.antorof1.flightreservationservice.flight.Flight;
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
