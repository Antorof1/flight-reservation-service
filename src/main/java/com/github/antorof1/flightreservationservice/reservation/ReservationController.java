package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservation", description = "The Reservation API")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves a single reservation by its ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new reservation", description = "Creates a temporary reservation (HOLD) for a seat.")
    @ApiResponse(responseCode = "201", description = "Reservation successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input or seat already held/reserved")
    @ApiResponse(responseCode = "404", description = "User or Seat not found")
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request.userId(), request.seatId());

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation", description = "Confirms a temporary reservation, changing its " +
        "status to RESERVED.")
    @ApiResponse(responseCode = "200", description = "Reservation successfully confirmed")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be confirmed in its current state")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.confirmReservation(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation",
        description = "Cancels a reservation, making the seat available again.")
    @ApiResponse(responseCode = "200", description = "Reservation successfully cancelled")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled in its current state")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.cancelReservation(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }
}
