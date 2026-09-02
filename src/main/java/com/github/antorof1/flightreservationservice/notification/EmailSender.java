package com.github.antorof1.flightreservationservice.notification;

public interface EmailSender {
    void send(EmailMessage emailMessage);
}
