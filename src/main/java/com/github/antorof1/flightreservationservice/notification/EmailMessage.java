package com.github.antorof1.flightreservationservice.notification;

public record EmailMessage(
    String to,
    String subject,
    String text
) {
}
