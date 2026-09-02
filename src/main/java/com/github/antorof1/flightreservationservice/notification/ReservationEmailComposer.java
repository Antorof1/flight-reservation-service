package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import org.springframework.stereotype.Component;

@Component
public class ReservationEmailComposer {
    public EmailMessage compose(ReservationEvent event) {
        return switch (event.type()) {
            case CONFIRMED -> confirmed(event);
            case CANCELLED -> cancelled(event);
        };
    }

    private EmailMessage confirmed(ReservationEvent event) {
        String subject = "Your flight %s is confirmed".formatted(event.flightNumber());

        String text = """
            Hi %s,
            
            Your seat is booked. Here are your details:
            
            %s
            
            Flight Reservation Service"""
            .formatted(event.passengerName(), details(event));

        return new EmailMessage(event.passengerEmail(), subject, text);
    }

    private EmailMessage cancelled(ReservationEvent event) {
        String subject = "Your reservation for flight %s was cancelled".formatted(event.flightNumber());

        String text = """
            Hi %s,
            
            Your reservation has been cancelled and the seat has been released:
            
            %s
            
            Flight Reservation Service"""
            .formatted(event.passengerName(), details(event));

        return new EmailMessage(event.passengerEmail(), subject, text);
    }

    private String details(ReservationEvent event) {
        return """
            Flight:    %s (%s -> %s)
            Departure: %s
            Seat:      %s"""
            .formatted(
                event.flightNumber(),
                event.departureAirport(),
                event.arrivalAirport(),
                event.departureTime(),
                event.seatNumber()
            );
    }
}
