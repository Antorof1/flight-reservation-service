package com.github.antorof1.flightreservationservice.notification.exception;

public abstract class EmailDeliveryException extends RuntimeException {
    protected EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
