package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import com.github.antorof1.flightreservationservice.seat.dto.SeatStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat", description = "The Seat API")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    @Operation(summary = "Get seats by flight ID",
        description = "Retrieves a list of seats for a specific flight, optionally filtered by status.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved seats")
    @ApiResponse(responseCode = "404", description = "Flight not found")
    public ResponseEntity<List<SeatResponse>> getSeats(
        @Parameter(description = "ID of the flight to retrieve seats for") @RequestParam Long flightId,
        @Parameter(description = "Optional seat status to filter by") @RequestParam(
            required = false) @Nullable SeatStatus status) {
        List<Seat> seats = seatService.getSeatsByFlightId(flightId, status);

        List<SeatResponse> seatsResponse = seats.stream().map(SeatResponse::fromEntity).toList();

        return ResponseEntity.ok(seatsResponse);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update seat status", description = "Updates the status of a specific seat.")
    @ApiResponse(responseCode = "200", description = "Successfully updated seat status")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Seat not found")
    public ResponseEntity<SeatResponse> updateSeatStatus(@PathVariable Long id,
                                                         @Valid @RequestBody SeatStatusUpdateRequest request) {
        Seat seat = seatService.updateSeatStatus(id, request.status());

        SeatResponse response = SeatResponse.fromEntity(seat);

        return ResponseEntity.ok(response);
    }
}
