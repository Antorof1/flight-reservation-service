package com.github.antorof1.flightreservationservice.factory;

import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEventType;

public final class ReservationEventTestDataFactory {
    public static final String PASSENGER_EMAIL = "john.doe@example.com";
    public static final String PASSENGER_NAME = "John Doe";
    public static final String FLIGHT_NUMBER = "FL-123";
    public static final String DEPARTURE_AIRPORT = "NYC";
    public static final String ARRIVAL_AIRPORT = "LAX";
    public static final String DEPARTURE_TIME = "05 Sep 2026, 09:30 +02:00";
    public static final String SEAT_NUMBER = "12A";

    private ReservationEventTestDataFactory() {
    }

    public static ReservationEvent event(ReservationEventType type) {
        return new ReservationEvent(
            type,
            PASSENGER_EMAIL,
            PASSENGER_NAME,
            FLIGHT_NUMBER,
            DEPARTURE_AIRPORT,
            ARRIVAL_AIRPORT,
            DEPARTURE_TIME,
            SEAT_NUMBER
        );
    }

    public static ReservationEvent confirmedEvent() {
        return event(ReservationEventType.CONFIRMED);
    }

    public static ReservationEvent cancelledEvent() {
        return event(ReservationEventType.CANCELLED);
    }
}
