package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.flight.dto.CreateFlightRequest;
import com.github.antorof1.flightreservationservice.flight.dto.FlightResponse;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Flight", description = "The Flight API")
public class FlightController {
    private final FlightService flightService;
    private final SeatService seatService;

    public FlightController(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }

    @GetMapping
    @Operation(summary = "Get all flights", description = "Retrieves a paginated list of all available flights.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved flights")
    public ResponseEntity<Page<FlightResponse>> getFlights(@ParameterObject @PageableDefault(
        sort = "departureTime") Pageable pageable) {
        Page<Flight> flights = flightService.getAllFlights(pageable);

        Page<FlightResponse> flightResponses = flights.map(FlightResponse::fromEntity);

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


    @GetMapping("/{id}/seats")
    @Operation(summary = "Get seats by flight ID",
        description = "Retrieves a list of seats for a specific flight, optionally filtered by status.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved seats")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    public ResponseEntity<List<SeatResponse>> getFlightSeats(
        @PathVariable Long id,
        @Parameter(description = "Optional seat status to filter by") @RequestParam(
            required = false) @Nullable SeatStatus status) {
        List<Seat> seats = seatService.getSeatsByFlightId(id, status);

        List<SeatResponse> seatsResponse = seats.stream().map(SeatResponse::fromEntity).toList();

        return ResponseEntity.ok(seatsResponse);
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
