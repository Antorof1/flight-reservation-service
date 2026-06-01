package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.AbstractIntegrationTest;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import com.github.antorof1.flightreservationservice.user.dto.CreateUserRequest;
import com.github.antorof1.flightreservationservice.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationIntegrationTest extends AbstractIntegrationTest {
    @Test
    @DisplayName("Should successfully complete a booking workflow: create user, reserve seat and confirm")
    void shouldCompleteBookingWorkflow() {
        Flight flight = flightFactory.createFlight("FL-123");

        CreateUserRequest userRequest = new CreateUserRequest("john.doe@example.com", "John Doe");

        UserResponse userResponse = testClient.post()
            .uri("/api/v1/users")
            .bodyValue(userRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(UserResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.email()).isEqualTo(userRequest.email());
        assertThat(userResponse.name()).isEqualTo(userRequest.name());

        Long userId = userResponse.id();

        List<SeatResponse> seatResponses = testClient.get()
            .uri(uriBuilder ->
                uriBuilder.path("/api/v1/seats")
                    .queryParam("flightId", flight.getId())
                    .queryParam("status", "AVAILABLE").build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<SeatResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        assertThat(seatResponses).isNotEmpty();

        Long seatId = seatResponses.getFirst().id();

        CreateReservationRequest reservationRequest = new CreateReservationRequest(userId, seatId);

        ReservationResponse reservationResponse = testClient.post()
            .uri("/api/v1/reservations")
            .bodyValue(reservationRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ReservationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(reservationResponse).isNotNull();
        assertThat(reservationResponse.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservationResponse.seat().status()).isEqualTo(SeatStatus.HELD);

        Long reservationId = reservationResponse.id();

        ReservationResponse confirmResponse = testClient.put()
            .uri("/api/v1/reservations/{id}/confirm", reservationId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ReservationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(confirmResponse).isNotNull();
        assertThat(confirmResponse.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(confirmResponse.seat().status()).isEqualTo(SeatStatus.BOOKED);
    }
}
