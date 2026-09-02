package com.github.antorof1.flightreservationservice.notification.exception;

public class TransientEmailException extends EmailDeliveryException {
    public TransientEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
