package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.flight.dto.CreateFlightRequest;
import com.github.antorof1.flightreservationservice.flight.dto.FlightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Flight", description = "The Flight API")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    @Operation(summary = "Get all flights", description = "Retrieves a list of all available flights.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved flights")
    public ResponseEntity<List<FlightResponse>> getFlights() {
        List<Flight> flights = flightService.getAllFlights();

        List<FlightResponse> flightResponses = flights.stream().map(FlightResponse::fromEntity).toList();

        return ResponseEntity.ok(flightResponses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Retrieves a single flight by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved flight")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);

        FlightResponse response = FlightResponse.fromEntity(flight);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new flight", description = "Creates a new flight with the provided details.")
    @ApiResponse(responseCode = "201", description = "Flight successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        Flight flight = request.toEntity();
        Flight savedFlight = flightService.createFlight(flight);

        FlightResponse response = FlightResponse.fromEntity(savedFlight);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
