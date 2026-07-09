package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.AbstractControllerTest;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.ReservationStatus;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest extends AbstractControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("GET /api/v1/users/reservations should return current user reservations")
    void shouldReturnUserReservations() throws Exception {
        User user = new User(
            "john.doe@example.com",
            "John Doe",
            "password123",
            UserRole.USER
        );
        user.setId(1L);

        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);

        Seat seat = new Seat(flight, "12A", SeatClass.ECONOMY, new BigDecimal("100.00"), SeatStatus.AVAILABLE);
        seat.setId(1L);

        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now()
            .plusMinutes(15));
        reservation.setId(1L);

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));

        when(userService.getUserById(1L)).thenReturn(user);
        when(reservationService.getAllReservationsByUser(eq(user), any(Pageable.class)))
            .thenReturn(reservationPage);

        mockMvc.perform(get("/api/v1/users/reservations")
                .with(authentication(mockAuth(user))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].userId").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }
}
