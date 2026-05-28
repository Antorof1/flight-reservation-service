package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.flight.dto.CreateFlightRequest;
import com.github.antorof1.flightreservationservice.flight.dto.FlightResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getFlights() {
        List<Flight> flights = flightService.getAllFlights();

        List<FlightResponse> flightResponses = flights.stream().map(FlightResponse::fromEntity).toList();

        return ResponseEntity.ok(flightResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);

        FlightResponse response = FlightResponse.fromEntity(flight);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        Flight flight = request.toEntity();
        Flight savedFlight = flightService.createFlight(flight);

        FlightResponse response = FlightResponse.fromEntity(savedFlight);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
