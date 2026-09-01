package com.github.antorof1.flightreservationservice.reservation.event;

public record ReservationEvent(
    ReservationEventType type,
    String passengerEmail,
    String passengerName,
    String flightNumber,
    String departureAirport,
    String arrivalAirport,
    String departureTime,
    String seatNumber
) {
}
