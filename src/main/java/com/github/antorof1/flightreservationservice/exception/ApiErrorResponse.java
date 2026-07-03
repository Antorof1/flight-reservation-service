package com.github.antorof1.flightreservationservice.exception;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
    @Schema(description = "When the error occurred", example = "2026-07-03T15:30:00Z")
    OffsetDateTime timestamp,

    @Schema(description = "HTTP status code", example = "404")
    int status,

    @Schema(description = "HTTP status description", example = "Not Found")
    String error,

    @Schema(description = "Detailed error message", example = "Reservation with ID 123 was not found")
    String message,

    @Schema(description = "The URI path that was requested", example = "/api/v1/reservations/123")
    String path,

    @ArraySchema(
        schema = @Schema(
            description = "Validation error",
            example = "seatNumber: must not be blank"
        )
    )
    List<String> details
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(OffsetDateTime.now(), status, error, message, path, List.of());
    }

    public ApiErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(OffsetDateTime.now(), status, error, message, path, details);
    }
}
