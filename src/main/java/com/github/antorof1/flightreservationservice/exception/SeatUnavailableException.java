package com.github.antorof1.flightreservationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatUnavailableException extends ReservationDomainException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
