package com.github.antorof1.flightreservationservice.seat.dto;

import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response object containing seat details")
public record SeatResponse(
    @Schema(description = "Unique identifier of the seat")
    Long id,

    @Schema(description = "The seat number", example = "12A")
    String seatNumber,

    @Schema(description = "The class of the seat")
    SeatClass seatClass,

    @Schema(description = "The price of the seat", example = "150.00")
    BigDecimal price,

    @Schema(description = "The current status of the seat")
    SeatStatus status
) {
    public static SeatResponse fromEntity(Seat seat) {
        return new SeatResponse(
            seat.getId(),
            seat.getSeatNumber(),
            seat.getSeatClass(),
            seat.getPrice(),
            seat.getStatus()
        );
    }
}
