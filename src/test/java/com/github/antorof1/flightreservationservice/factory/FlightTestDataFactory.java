package com.github.antorof1.flightreservationservice.factory;

import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.flight.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class FlightTestDataFactory {
    @Autowired
    private FlightService flightService;

    public Flight createFlight(String flightNumber) {
        Flight flight = new Flight(
            flightNumber,
            "NYC",
            "LAX",
            OffsetDateTime.now().plusDays(5),
            OffsetDateTime.now().plusDays(5).plusHours(6)
        );

        return flightService.createFlight(flight);
    }
}
