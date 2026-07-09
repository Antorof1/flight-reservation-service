package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.security.JwtPrincipal;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationSecurityTest {
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationSecurity reservationSecurity;

    @Test
    @DisplayName("An ADMIN principal can access any reservation without looking it up")
    void adminPrincipal_CanAccessAnyReservation() {
        JwtPrincipal admin = new JwtPrincipal(99L, UserRole.ADMIN);

        boolean canAccess = reservationSecurity.canAccessReservation(admin, 1L);

        assertThat(canAccess).isTrue();
        verify(reservationService, never()).getReservationById(anyLong());
    }

    @Test
    @DisplayName("A USER principal can access their own reservation")
    void userPrincipal_CanAccessOwnReservation() {
        Long userId = 1L;
        Reservation reservation = reservationOwnedBy(userId);
        JwtPrincipal owner = new JwtPrincipal(userId, UserRole.USER);

        when(reservationService.getReservationById(10L)).thenReturn(reservation);

        boolean canAccess = reservationSecurity.canAccessReservation(owner, 10L);

        assertThat(canAccess).isTrue();
    }

    @Test
    @DisplayName("A USER principal cannot access another user's reservation")
    void userPrincipal_CannotAccessOthersReservation() {
        Reservation reservation = reservationOwnedBy(1L);
        JwtPrincipal otherUser = new JwtPrincipal(2L, UserRole.USER);

        when(reservationService.getReservationById(10L)).thenReturn(reservation);

        boolean canAccess = reservationSecurity.canAccessReservation(otherUser, 10L);

        assertThat(canAccess).isFalse();
    }

    @Test
    @DisplayName("A non-existent reservation propagates ResourceNotFoundException for non-admin principals")
    void nonExistentReservation_PropagatesException() {
        JwtPrincipal user = new JwtPrincipal(1L, UserRole.USER);

        when(reservationService.getReservationById(404L))
            .thenThrow(new ResourceNotFoundException("Reservation not found with id: 404"));

        assertThatThrownBy(() -> reservationSecurity.canAccessReservation(user, 404L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private Reservation reservationOwnedBy(Long userId) {
        User user = new User("owner@example.com", "Owner", "password123", UserRole.USER);
        user.setId(userId);

        Flight flight = new Flight(
            "FL123",
            "JFK",
            "LAX",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusHours(5)
        );
        flight.setId(1L);

        Seat seat = new Seat(flight, "12A", SeatClass.ECONOMY, new BigDecimal("100.00"), SeatStatus.HELD);
        seat.setId(1L);

        Reservation reservation = new Reservation(
            user,
            seat,
            ReservationStatus.PENDING,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now().plusMinutes(10)
        );
        reservation.setId(10L);

        return reservation;
    }
}
