package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import com.github.antorof1.flightreservationservice.seat.dto.SeatStatusUpdateRequest;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeats(@RequestParam Long flightId, @RequestParam(
        required = false) @Nullable SeatStatus status) {
        List<Seat> seats = seatService.getSeatsByFlightId(flightId, status);

        List<SeatResponse> seatsResponse = seats.stream().map(SeatResponse::fromEntity).toList();

        return ResponseEntity.ok(seatsResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SeatResponse> updateSeatStatus(@PathVariable Long id,
                                                         @Valid @RequestBody SeatStatusUpdateRequest request) {
        Seat seat = seatService.updateSeatStatus(id, request.status());

        SeatResponse response = SeatResponse.fromEntity(seat);

        return ResponseEntity.ok(response);
    }
}
