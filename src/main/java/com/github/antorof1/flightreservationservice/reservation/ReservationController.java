package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request.userId(), request.seatId());

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.confirmReservation(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.cancelReservation(id);

        ReservationResponse response = ReservationResponse.fromEntity(reservation);

        return ResponseEntity.ok(response);
    }
}
