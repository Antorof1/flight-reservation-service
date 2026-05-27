package com.github.antorof1.flightreservationservice.reservation.dto;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(
    @NotNull
    Long userId,

    @NotNull
    Long seatId
) {
}
