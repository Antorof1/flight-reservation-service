package com.github.antorof1.flightreservationservice;

import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.flight.dto.CreateFlightRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.seat.dto.SeatStatusUpdateRequest;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityIntegrationTest extends AbstractIntegrationTest {
    @Test
    @DisplayName("GET /api/v1/flights is public and does not require an Authorization header")
    void publicEndpoint_AccessibleWithoutToken() {
        flightFactory.createFlight("FL-100");

        testClient.get()
            .uri("/api/v1/flights")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("A secured endpoint without an Authorization header returns 401")
    void securedEndpoint_NoToken_ReturnsUnauthorized() {
        testClient.get()
            .uri("/api/v1/reservations/1")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("A secured endpoint with a malformed token returns 401")
    void securedEndpoint_MalformedToken_ReturnsUnauthorized() {
        testClient.get()
            .uri("/api/v1/reservations/1")
            .header("Authorization", "Bearer not-a-real-jwt")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /api/v1/flights with a USER token returns 403")
    void adminEndpoint_UserToken_ReturnsForbidden() {
        User user = userFactory.createUser("regular.user@example.com");
        String token = tokenFor(user);

        CreateFlightRequest request = new CreateFlightRequest(
            "FL-200",
            "JFK",
            "LAX",
            OffsetDateTime.now().plusDays(1),
            OffsetDateTime.now().plusDays(1).plusHours(5)
        );

        testClient.post()
            .uri("/api/v1/flights")
            .header("Authorization", "Bearer " + token)
            .bodyValue(request)
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("POST /api/v1/flights with an ADMIN token returns 201")
    void adminEndpoint_AdminToken_ReturnsCreated() {
        User admin = userRepository.save(new User("admin@example.com", "Admin", "password123", UserRole.ADMIN));
        String token = tokenFor(admin);

        CreateFlightRequest request = new CreateFlightRequest(
            "FL-201",
            "JFK",
            "LAX",
            OffsetDateTime.now().plusDays(1),
            OffsetDateTime.now().plusDays(1).plusHours(5)
        );

        testClient.post()
            .uri("/api/v1/flights")
            .header("Authorization", "Bearer " + token)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated();
    }

    @Test
    @DisplayName("PATCH /api/v1/seats/{id}/status with a USER token returns 403")
    void seatStatusEndpoint_UserToken_ReturnsForbidden() {
        Flight flight = flightFactory.createFlight("FL-202");
        Seat seat = seatRepository.findByFlightId(flight.getId()).getFirst();

        User user = userFactory.createUser("regular.user2@example.com");
        String token = tokenFor(user);

        SeatStatusUpdateRequest request = new SeatStatusUpdateRequest(SeatStatus.HELD);

        testClient.patch()
            .uri("/api/v1/seats/{id}/status", seat.getId())
            .header("Authorization", "Bearer " + token)
            .bodyValue(request)
            .exchange()
            .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("A USER token cannot access another user's reservation")
    void reservation_OtherUsersToken_ReturnsForbidden() {
        Flight flight = flightFactory.createFlight("FL-203");
        Seat seat = seatRepository.findByFlightId(flight.getId()).getFirst();

        User owner = userFactory.createUser("owner@example.com");
        User intruder = userFactory.createUser("intruder@example.com");

        ReservationResponse reservationResponse = testClient.post()
            .uri("/api/v1/reservations")
            .header("Authorization", "Bearer " + tokenFor(owner))
            .bodyValue(new CreateReservationRequest(seat.getId()))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ReservationResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(reservationResponse).isNotNull();
        Long reservationId = reservationResponse.id();

        testClient.get()
            .uri("/api/v1/reservations/{id}", reservationId)
            .header("Authorization", "Bearer " + tokenFor(intruder))
            .exchange()
            .expectStatus().isForbidden();
    }
}
