package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.AbstractIntegrationTest;
import com.github.antorof1.flightreservationservice.config.RabbitConfig;
import com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.ReservationStatus;
import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = "app.notifications.mode=ALL")
class NotificationIntegrationTest extends AbstractIntegrationTest {
    private static final String FLIGHT_NUMBER = "FL-123";
    private static final String PASSENGER_EMAIL = "john.doe@example.com";
    private static final String PASSENGER_NAME = "John Doe";

    @MockitoBean
    private Resend resend;

    @Autowired
    private SeatService seatService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Emails emails;

    @BeforeEach
    void setUpResend() throws ResendException {
        emails = mock(Emails.class);

        when(resend.emails()).thenReturn(emails);
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(new CreateEmailResponse("email-id"));
    }

    private CreateEmailOptions awaitSentEmail() throws ResendException {
        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails, timeout(10_000)).send(captor.capture());

        return captor.getValue();
    }

    private ReservationResponse createReservation(User user) {
        Flight flight = flightFactory.createFlight(FLIGHT_NUMBER);
        Seat seat = seatService.getSeatsByFlightId(flight.getId(), SeatStatus.AVAILABLE).getFirst();

        return testClient.post()
            .uri("/api/v1/reservations")
            .bodyValue(new CreateReservationRequest(seat.getId()))
            .header("Authorization", "Bearer " + tokenFor(user))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ReservationResponse.class)
            .returnResult()
            .getResponseBody();
    }

    @Test
    @DisplayName("Should email the passenger after a reservation is confirmed")
    void confirmReservation_SendsConfirmationEmail() throws ResendException {
        User user = userFactory.createUser(PASSENGER_EMAIL, PASSENGER_NAME);
        ReservationResponse reservation = createReservation(user);

        testClient.put()
            .uri("/api/v1/reservations/{id}/confirm", reservation.id())
            .header("Authorization", "Bearer " + tokenFor(user))
            .exchange()
            .expectStatus().isOk()
            .expectBody(ReservationResponse.class)
            .returnResult();

        CreateEmailOptions sent = awaitSentEmail();

        assertThat(sent.getTo()).containsExactly(PASSENGER_EMAIL);
        assertThat(sent.getFrom()).isEqualTo("no-reply@test.example");
        assertThat(sent.getSubject()).contains(FLIGHT_NUMBER).containsIgnoringCase("confirmed");
        assertThat(sent.getText())
            .contains(PASSENGER_NAME)
            .contains(reservation.seat().seatNumber());
    }

    @Test
    @DisplayName("Should email the passenger after a reservation is cancelled")
    void cancelReservation_SendsCancellationEmail() throws ResendException {
        User user = userFactory.createUser(PASSENGER_EMAIL, PASSENGER_NAME);
        ReservationResponse reservation = createReservation(user);

        testClient.put()
            .uri("/api/v1/reservations/{id}/cancel", reservation.id())
            .header("Authorization", "Bearer " + tokenFor(user))
            .exchange()
            .expectStatus().isOk()
            .expectBody(ReservationResponse.class)
            .returnResult();

        CreateEmailOptions sent = awaitSentEmail();

        assertThat(sent.getTo()).containsExactly(PASSENGER_EMAIL);
        assertThat(sent.getSubject()).contains(FLIGHT_NUMBER).containsIgnoringCase("cancelled");
        assertThat(sent.getText())
            .contains(reservation.seat().seatNumber());
    }

    @Test
    @DisplayName("Should not email anyone when the confirmation is rejected")
    void confirmReservation_Rejected_SendsNothing() throws ResendException {
        User user = userFactory.createUser(PASSENGER_EMAIL, PASSENGER_NAME);
        ReservationResponse reservation = createReservation(user);

        redisTemplate.delete("seat:lock:" + reservation.seat().id());

        testClient.put()
            .uri("/api/v1/reservations/{id}/confirm", reservation.id())
            .header("Authorization", "Bearer " + tokenFor(user))
            .exchange()
            .expectStatus().isBadRequest();

        verify(emails, after(1_000).never()).send(any(CreateEmailOptions.class));
        assertThat(reservationRepository.findById(reservation.id()).orElseThrow().getStatus())
            .isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("Should route an event published on the reservation exchange to the email listener")
    void reservationEvent_RoutedFromExchangeToListener() throws ResendException {
        ReservationEvent event = ReservationEventTestDataFactory.confirmedEvent();

        rabbitTemplate.convertAndSend(
            RabbitConfig.RESERVATION_EXCHANGE,
            RabbitConfig.RESERVATION_CONFIRMED_KEY,
            event
        );

        CreateEmailOptions sent = awaitSentEmail();

        assertThat(sent.getTo()).containsExactly(event.passengerEmail());
        assertThat(sent.getSubject()).contains(event.flightNumber());
        assertThat(sent.getText())
            .contains(event.seatNumber())
            .contains(event.departureTime());
    }

    @Test
    @DisplayName("Should retry a transient Resend failure before giving up")
    void transientResendFailure_IsRetried() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class)))
            .thenThrow(new ResendException(503, """
                {"name": "service_unavailable", "message": "try again"}"""))
            .thenReturn(new CreateEmailResponse("email-id"));

        rabbitTemplate.convertAndSend(
            RabbitConfig.RESERVATION_EXCHANGE,
            RabbitConfig.RESERVATION_CONFIRMED_KEY,
            ReservationEventTestDataFactory.confirmedEvent()
        );

        verify(emails, timeout(10_000).times(2)).send(any(CreateEmailOptions.class));
        verify(emails, after(500).times(2)).send(any(CreateEmailOptions.class));
    }
}
