package com.github.antorof1.flightreservationservice.reservation.dto;

import com.github.antorof1.flightreservationservice.flight.dto.FlightResponse;
import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Response object containing reservation details")
public record ReservationResponse(
    @Schema(description = "Unique identifier of the reservation")
    Long id,

    @Schema(description = "ID of the user who made the reservation")
    Long userId,

    @Schema(description = "Details of the reserved seat")
    SeatResponse seat,

    @Schema(description = "Details of the flight")
    FlightResponse flight,

    @Schema(description = "Current status of the reservation")
    ReservationStatus status,

    @Schema(description = "When the reservation was created")
    OffsetDateTime createdAt,

    @Schema(description = "When the reservation (HOLD) expires")
    OffsetDateTime expiresAt
) {
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getUser().getId(),
            SeatResponse.fromEntity(reservation.getSeat()),
            FlightResponse.fromEntity(reservation.getSeat().getFlight()),
            reservation.getStatus(),
            reservation.getCreatedAt(),
            reservation.getExpiresAt()
        );
    }
}
