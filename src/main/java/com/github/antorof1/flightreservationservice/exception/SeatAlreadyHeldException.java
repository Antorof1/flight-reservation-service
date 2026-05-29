package com.github.antorof1.flightreservationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatAlreadyHeldException extends ReservationDomainException {
    public SeatAlreadyHeldException(String message) {
        super(message);
    }
}
