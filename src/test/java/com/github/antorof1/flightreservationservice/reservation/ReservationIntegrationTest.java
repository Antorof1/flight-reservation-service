package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.AbstractIntegrationTest;
import com.github.antorof1.flightreservationservice.auth.dto.AuthResponse;
import com.github.antorof1.flightreservationservice.auth.dto.RegisterRequest;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatResponse;
import com.github.antorof1.flightreservationservice.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private SeatService seatService;

    @Test
    @DisplayName("Should successfully complete a booking workflow: create user, reserve seat and confirm")
    void shouldCompleteBookingWorkflow() {
        Flight flight = flightFactory.createFlight("FL-123");

        RegisterRequest registerRequest = new RegisterRequest(
            "john.doe@example.com",
            "John Doe",
            "password123"
        );

        AuthResponse authResponse = testClient.post()
            .uri("/api/v1/auth/register")
            .bodyValue(registerRequest)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(AuthResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.email()).isEqualTo(registerRequest.email());
        assertThat(authResponse.name()).isEqualTo(registerRequest.name());

        List<SeatResponse> seatResponses = testClient.get()
            .uri(uriBuilder ->
                uriBuilder.path("/api/v1/flights/{id}/seats")
                    .queryParam("status", "AVAILABLE")
                    .build(flight.getId())
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<SeatResponse>>() {
            })
            .returnResult()
            .getResponseBody();

        assertThat(seatResponses).isNotEmpty();

        Long seatId = seatResponses.getFirst().id();

        CreateReservationRequest reservationRequest = new CreateReservationRequest(seatId);

        ReservationResponse reservationResponse = testClient.post()
            .uri("/api/v1/reservations")
            .bodyValue(reservationRequest)
            .header("Authorization", "Bearer " + authResponse.token())
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
            .header("Authorization", "Bearer " + authResponse.token())
            .exchange()
            .expectStatus().isOk()
            .expectBody(ReservationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(confirmResponse).isNotNull();
        assertThat(confirmResponse.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(confirmResponse.seat().status()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    @DisplayName("Should allow only one reservation and reject others when multiple users attempt to reserve the same" +
        " seat simultaneously")
    void shouldAllowOnlyOneReservationWhenMultipleUsersReserveSameSeatConcurrently() {
        final int REQUEST_COUNT = 50;

        Flight flight = flightFactory.createFlight("FL-123");
        List<User> users = userFactory.createUsers(REQUEST_COUNT);
        Seat seat = seatService.getSeatsByFlightId(flight.getId(), SeatStatus.AVAILABLE).getFirst();

        Queue<HttpStatusCode> responseStatuses = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (User user : users) {
                CreateReservationRequest request = new CreateReservationRequest(seat.getId());
                String token = tokenFor(user);

                executor.submit(() -> {
                    try {
                        latch.await();

                        testClient.post()
                            .uri("/api/v1/reservations")
                            .bodyValue(request)
                            .header("Authorization", "Bearer " + token)
                            .exchange()
                            .expectBody()
                            .consumeWith(result -> {
                                responseStatuses.add(result.getStatus());
                            });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            latch.countDown();
        }

        long successCount = responseStatuses.stream().filter(HttpStatusCode::is2xxSuccessful).count();
        long failureCount = responseStatuses.stream().filter(HttpStatusCode::isError).count();

        assertThat(responseStatuses).hasSize(REQUEST_COUNT);
        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(REQUEST_COUNT - 1);
    }

    @Test
    @DisplayName("Should prevent double-booking and maintain correct inventory status when multiple users reserve " +
        "different seats simultaneously")
    void shouldMaintainInventoryIntegrityWhenMultipleUsersReserveDifferentSeatsConcurrently() {
        Flight flight = flightFactory.createFlight("FL-123");
        List<Seat> seats = seatService.getSeatsByFlightId(flight.getId(), SeatStatus.AVAILABLE);

        int requestCount = seats.size();

        List<User> users = userFactory.createUsers(requestCount);

        Queue<HttpStatusCode> responseStatuses = new ConcurrentLinkedQueue<>();
        Queue<ReservationResponse> successfulReservations = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < requestCount; i++) {
                Seat seat = seats.get(i);
                User user = users.get(i);

                CreateReservationRequest request = new CreateReservationRequest(seat.getId());
                String token = tokenFor(user);

                executor.submit(() -> {
                    try {
                        latch.await();

                        testClient.post()
                            .uri("/api/v1/reservations")
                            .bodyValue(request)
                            .header("Authorization", "Bearer " + token)
                            .exchange()
                            .expectBody(ReservationResponse.class)
                            .consumeWith(result -> {
                                responseStatuses.add(result.getStatus());

                                if (result.getStatus().is2xxSuccessful() && result.getResponseBody() != null) {
                                    successfulReservations.add(result.getResponseBody());
                                }
                            });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }

            latch.countDown();
        }

        long successCount = responseStatuses.stream().filter(HttpStatusCode::is2xxSuccessful).count();

        assertThat(responseStatuses).hasSize(requestCount);
        assertThat(successCount).isEqualTo(requestCount);
        assertThat(successfulReservations).hasSize(requestCount);

        for (ReservationResponse response : successfulReservations) {
            assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);
            assertThat(response.seat().status()).isEqualTo(SeatStatus.HELD);
        }

        List<Seat> remainingSeats = seatService.getSeatsByFlightId(flight.getId(), SeatStatus.AVAILABLE);

        assertThat(remainingSeats).isEmpty();
    }
}
