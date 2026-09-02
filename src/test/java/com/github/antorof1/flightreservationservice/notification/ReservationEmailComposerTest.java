package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory.DEPARTURE_TIME;
import static com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory.FLIGHT_NUMBER;
import static com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory.PASSENGER_EMAIL;
import static com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory.PASSENGER_NAME;
import static com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory.SEAT_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;

class ReservationEmailComposerTest {
    private final ReservationEmailComposer composer = new ReservationEmailComposer();

    @Test
    @DisplayName("Should compose a confirmation email addressed to the passenger")
    void compose_Confirmed() {
        ReservationEvent event = ReservationEventTestDataFactory.confirmedEvent();

        EmailMessage message = composer.compose(event);

        assertThat(message.to()).isEqualTo(PASSENGER_EMAIL);
        assertThat(message.subject()).contains(FLIGHT_NUMBER).containsIgnoringCase("confirmed");
        assertThat(message.text())
            .contains(PASSENGER_NAME)
            .contains(FLIGHT_NUMBER)
            .contains("NYC")
            .contains("LAX")
            .contains(DEPARTURE_TIME)
            .contains(SEAT_NUMBER)
            .contains("Your seat is booked");
    }

    @Test
    @DisplayName("Should compose a cancellation email addressed to the passenger")
    void compose_Cancelled() {
        ReservationEvent event = ReservationEventTestDataFactory.cancelledEvent();

        EmailMessage message = composer.compose(event);

        assertThat(message.to()).isEqualTo(PASSENGER_EMAIL);
        assertThat(message.subject()).contains(FLIGHT_NUMBER).containsIgnoringCase("cancelled");
        assertThat(message.text())
            .contains(PASSENGER_NAME)
            .contains(FLIGHT_NUMBER)
            .contains(SEAT_NUMBER)
            .contains("has been cancelled")
            .contains("seat has been released");
    }
}
