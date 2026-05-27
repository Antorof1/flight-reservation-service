package com.github.antorof1.flightreservationservice.reservation.dto;

import com.github.antorof1.flightreservationservice.flight.dto.FlightResponse;
import com.github.antorof1.flightreservationservice.reservation.ReservationStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;

import java.time.OffsetDateTime;

public record ReservationResponse(
    Long id,
    Long userId,
    SeatResponse seat,
    FlightResponse flight,
    ReservationStatus status,
    OffsetDateTime cratedAt,
    OffsetDateTime expiresAt
) {
}
