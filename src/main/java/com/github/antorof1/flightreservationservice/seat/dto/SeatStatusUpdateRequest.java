package com.github.antorof1.flightreservationservice.seat.dto;

import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for updating seat status")
public record SeatStatusUpdateRequest(
    @NotNull
    @Schema(description = "The new status of the seat")
    SeatStatus status
) {
}
