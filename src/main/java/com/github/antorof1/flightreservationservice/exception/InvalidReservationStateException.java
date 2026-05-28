package com.github.antorof1.flightreservationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReservationStateException extends ReservationDomainException {
    public InvalidReservationStateException(String message) {
        super(message);
    }
}
