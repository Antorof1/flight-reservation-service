package com.github.antorof1.flightreservationservice.notification.exception;

public class PermanentEmailException extends EmailDeliveryException {
    public PermanentEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
