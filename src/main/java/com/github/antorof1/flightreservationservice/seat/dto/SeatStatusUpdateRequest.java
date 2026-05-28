package com.github.antorof1.flightreservationservice.seat.dto;

import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import jakarta.validation.constraints.NotNull;

public record SeatStatusUpdateRequest(@NotNull SeatStatus status) {
}
