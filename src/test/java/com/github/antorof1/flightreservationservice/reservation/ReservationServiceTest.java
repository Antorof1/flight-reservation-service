package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.exception.InvalidReservationStateException;
import com.github.antorof1.flightreservationservice.exception.SeatAlreadyHeldException;
import com.github.antorof1.flightreservationservice.exception.SeatUnavailableException;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserRole;
import com.github.antorof1.flightreservationservice.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.startsWith;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatService seatService;

    @Mock
    private UserService userService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Seat seat;

    @BeforeEach
    void setUp() {
        user = new User(
            "test@example.com",
            "Test User",
            "password123",
            UserRole.USER
        );
        user.setId(1L);
        seat = new Seat();
        seat.setId(1L);
        seat.setStatus(SeatStatus.AVAILABLE);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should successfully create a reservation")
    void createReservation_Success() {
        Long userId = 1L;
        Long seatId = 1L;

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES))).thenReturn(true);
        when(seatService.getSeatById(seatId)).thenReturn(seat);
        when(userService.getUserById(userId)).thenReturn(user);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation reservation = reservationService.createReservation(userId, seatId);

        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getSeat()).isEqualTo(seat);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        verify(valueOperations).setIfAbsent(contains("seat:lock:"), anyString(), eq(10L), eq(TimeUnit.MINUTES));
        verify(seatService).updateSeatStatus(seatId, SeatStatus.HELD);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Should throw SeatAlreadyHeldException when Redis lock fails")
    void createReservation_LockFails_ThrowsException() {
        Long userId = 1L;
        Long seatId = 1L;

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES))).thenReturn(false);

        assertThatThrownBy(() -> reservationService.createReservation(userId, seatId))
            .isInstanceOf(SeatAlreadyHeldException.class)
            .hasMessageContaining("Seat is currently held");

        verify(valueOperations).setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES));
        verifyNoInteractions(seatService, userService, reservationRepository);
    }

    @Test
    @DisplayName("Should throw SeatUnavailableException when seat status is not AVAILABLE")
    void createReservation_SeatNotAvailable_ThrowsException() {
        Long userId = 1L;
        Long seatId = 1L;
        seat.setStatus(SeatStatus.BOOKED);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.MINUTES))).thenReturn(true);
        when(seatService.getSeatById(seatId)).thenReturn(seat);

        assertThatThrownBy(() -> reservationService.createReservation(userId, seatId))
            .isInstanceOf(SeatUnavailableException.class)
            .hasMessageContaining("Requested seat is no longer available");

        verify(redisTemplate).delete(startsWith("seat:lock:"));
        verify(seatService, never()).updateSeatStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Should successfully confirm a reservation")
    void confirmReservation_Success() {
        Long reservationId = 1L;
        UUID lockToken = UUID.randomUUID();
        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, lockToken, OffsetDateTime.now(), OffsetDateTime.now()
            .plusMinutes(10));
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(valueOperations.get(anyString())).thenReturn(lockToken.toString());

        Reservation confirmed = reservationService.confirmReservation(reservationId);

        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(seatService).updateSeatStatus(seat.getId(), SeatStatus.BOOKED);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("Should throw InvalidReservationStateException when confirmation expires")
    void confirmReservation_TokenMismatch_ThrowsException() {
        Long reservationId = 1L;
        UUID lockToken = UUID.randomUUID();
        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, lockToken, OffsetDateTime.now(), OffsetDateTime.now()
            .plusMinutes(10));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(valueOperations.get(anyString())).thenReturn("wrong-token");

        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId))
            .isInstanceOf(InvalidReservationStateException.class)
            .hasMessageContaining("Reservation has expired already");

        verify(seatService, never()).updateSeatStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Should successfully cancel a reservation")
    void cancelReservation_Success() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now()
            .plusMinutes(10));
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        Reservation cancelled = reservationService.cancelReservation(reservationId);

        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(seatService).updateSeatStatus(seat.getId(), SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should throw InvalidReservationStateException when cancelling an already-expired reservation")
    void cancelReservation_AlreadyExpired_ThrowsException() {
        Long reservationId = 1L;
        Reservation reservation = new Reservation(
            user,
            seat,
            ReservationStatus.EXPIRED,
            UUID.randomUUID(),
            OffsetDateTime.now().minusMinutes(20),
            OffsetDateTime.now().minusMinutes(10)
        );
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId))
            .isInstanceOf(InvalidReservationStateException.class)
            .hasMessageContaining("Reservation has already expired");

        verify(seatService, never()).updateSeatStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Should cleanup expired reservation and release seat")
    void cleanUpExpiredReservation_ReleasesSeat() {
        seat.setStatus(SeatStatus.HELD);
        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, UUID.randomUUID(), OffsetDateTime.now()
            .minusMinutes(20), OffsetDateTime.now().minusMinutes(10));

        reservationService.cleanUpExpiredReservation(reservation);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        verify(seatService).updateSeatStatus(seat.getId(), SeatStatus.AVAILABLE);
        verify(reservationRepository).save(reservation);
    }
}
