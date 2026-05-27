package com.github.antorof1.flightreservationservice.seat.dto;

import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;

import java.math.BigDecimal;

public record SeatResponse(
    Long id,
    String seatNumber,
    SeatClass seatClass,
    BigDecimal price,
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
