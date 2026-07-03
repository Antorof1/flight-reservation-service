package com.github.antorof1.flightreservationservice.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for creating a new reservation")
public record CreateReservationRequest(
    @NotNull
    @Schema(description = "ID of the seat to reserve", example = "101")
    Long seatId
) {
}
