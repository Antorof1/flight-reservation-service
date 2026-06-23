package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.dto.CreateReservationRequest;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    private static final OffsetDateTime CREATED_AT = OffsetDateTime.parse("2026-01-01T10:00:00Z");
    private static final OffsetDateTime EXPIRES_AT = OffsetDateTime.parse("2026-01-01T10:10:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private User user;
    private Seat seat;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        user = new User(
            "john.doe@example.com",
            "John Doe",
            "password123",
            UserRole.USER
        );
        user.setId(1L);

        Flight flight = new Flight(
            "FL123",
            "JFK",
            "LAX",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusHours(5)
        );
        flight.setId(1L);

        seat = new Seat(
            flight,
            "12A",
            SeatClass.ECONOMY,
            new BigDecimal("100.00"),
            SeatStatus.HELD
        );
        seat.setId(1L);

        reservation = new Reservation(
            user,
            seat,
            ReservationStatus.PENDING,
            UUID.randomUUID(),
            CREATED_AT,
            EXPIRES_AT
        );
        reservation.setId(1L);
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{id} should return a reservation when it exists")
    void shouldReturnReservationById() throws Exception {
        when(reservationService.getReservationById(reservation.getId())).thenReturn(reservation);

        mockMvc.perform(get("/api/v1/reservations/" + reservation.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reservation.getId()))
            .andExpect(jsonPath("$.userId").value(user.getId()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/reservations/{id} should return 404 when reservation not found")
    void shouldReturn404WhenReservationNotFound() throws Exception {
        when(reservationService.getReservationById(99L)).thenThrow(new ResourceNotFoundException("Reservation not found"));

        mockMvc.perform(get("/api/v1/reservations/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Reservation not found"));
    }

    @Test
    @DisplayName("POST /api/v1/reservations should create a new reservation")
    void shouldCreateReservation() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest(user.getId(), seat.getId());

        when(reservationService.createReservation(user.getId(), seat.getId())).thenReturn(reservation);

        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(reservation.getId()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/v1/reservations/{id}/confirm should confirm reservation")
    void shouldConfirmReservation() throws Exception {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.confirmReservation(reservation.getId())).thenReturn(reservation);

        mockMvc.perform(put("/api/v1/reservations/" + reservation.getId() + "/confirm"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/v1/reservations/{id}/cancel should cancel reservation")
    void shouldCancelReservation() throws Exception {
        reservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.cancelReservation(reservation.getId())).thenReturn(reservation);

        mockMvc.perform(put("/api/v1/reservations/" + reservation.getId() + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
