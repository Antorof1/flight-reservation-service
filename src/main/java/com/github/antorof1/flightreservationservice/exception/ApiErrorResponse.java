package com.github.antorof1.flightreservationservice.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> details
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(null, status, error, message, path, List.of());
    }

    public ApiErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(null, status, error, message, path, details);
    }

    public ApiErrorResponse {
    }
}
