package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import com.github.antorof1.flightreservationservice.seat.dto.SeatStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seat", description = "The Seat API")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
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
