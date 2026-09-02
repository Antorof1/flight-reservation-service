package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.config.RabbitConfig;
import com.github.antorof1.flightreservationservice.exception.InvalidReservationStateException;
import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.exception.SeatAlreadyHeldException;
import com.github.antorof1.flightreservationservice.exception.SeatUnavailableException;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEventType;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final String SEAT_LOCK_PREFIX = "seat:lock:";
    private static final long HOLD_MINUTES = 10;
    private static final DateTimeFormatter DEPARTURE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm XXX", Locale.ROOT);

    private final ReservationRepository reservationRepository;
    private final SeatService seatService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final AmqpTemplate amqpTemplate;

    public ReservationService(ReservationRepository reservationRepository, SeatService seatService,
                              UserService userService, StringRedisTemplate redisTemplate, AmqpTemplate amqpTemplate) {
        this.reservationRepository = reservationRepository;
        this.seatService = seatService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.amqpTemplate = amqpTemplate;
    }

    @Transactional
    public Reservation createReservation(Long userId, Long seatId) {
        String seatLockKey = SEAT_LOCK_PREFIX + seatId;

        UUID lockToken = UUID.randomUUID();

        Boolean isLocked = redisTemplate.opsForValue()
            .setIfAbsent(seatLockKey, lockToken.toString(), HOLD_MINUTES, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isLocked)) {
            throw new SeatAlreadyHeldException("Seat is currently held by another user");
        }

        try {
            Seat seat = seatService.getSeatById(seatId);

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new SeatUnavailableException("Requested seat is no longer available");
            }

            seatService.updateSeatStatus(seatId, SeatStatus.HELD);

            Reservation reservation = new Reservation(
                userService.getUserById(userId),
                seat,
                ReservationStatus.PENDING,
                lockToken,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(HOLD_MINUTES)
            );

            return reservationRepository.save(reservation);
        } catch (Exception e) {
            redisTemplate.delete(seatLockKey);
            throw e;
        }
    }

    public Page<Reservation> getAllReservationsByUser(User user, Pageable pageable) {
        return reservationRepository.findAllByUser(user, pageable);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    @Transactional
    public Reservation confirmReservation(Long id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidReservationStateException(
                "Reservation cannot be confirmed because it is in status: " + reservation.getStatus());
        }

        String seatLockKey = SEAT_LOCK_PREFIX + reservation.getSeat().getId();
        String lockToken = redisTemplate.opsForValue().get(seatLockKey);

        if (lockToken == null || !lockToken.equals(reservation.getLockToken().toString())) {
            throw new InvalidReservationStateException("Reservation has expired already");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        seatService.updateSeatStatus(reservation.getSeat().getId(), SeatStatus.BOOKED);

        redisTemplate.delete(seatLockKey);

        publishAfterCommit(RabbitConfig.RESERVATION_CONFIRMED_KEY,
            toEvent(ReservationEventType.CONFIRMED, reservation));

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long id) {
        Reservation reservation = getReservationById(id);


        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationStateException("Reservation is already cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new InvalidReservationStateException("Reservation has already expired");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING
            && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidReservationStateException(
                "Reservation cannot be cancelled because it is in status: " + reservation.getStatus());
        }

        if (reservation.getStatus() == ReservationStatus.PENDING &&
            reservation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidReservationStateException("Reservation is expired");
        }

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            String seatLockKey = SEAT_LOCK_PREFIX + reservation.getSeat().getId();
            String lockToken = redisTemplate.opsForValue().get(seatLockKey);

            if (lockToken != null && lockToken.equals(reservation.getLockToken().toString())) {
                redisTemplate.delete(seatLockKey);
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        seatService.updateSeatStatus(reservation.getSeat().getId(), SeatStatus.AVAILABLE);

        publishAfterCommit(RabbitConfig.RESERVATION_CANCELLED_KEY,
            toEvent(ReservationEventType.CANCELLED, reservation));

        return reservation;
    }

    @Transactional
    public void cleanUpExpiredReservations() {
        List<Reservation> reservations = reservationRepository
            .findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, OffsetDateTime.now());

        for (Reservation reservation : reservations) {
            cleanUpExpiredReservation(reservation);
        }
    }

    @Transactional
    public void cleanUpExpiredReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.EXPIRED);

        Seat seat = reservation.getSeat();

        if (seat.getStatus() == SeatStatus.HELD) {
            seatService.updateSeatStatus(seat.getId(), SeatStatus.AVAILABLE);
        }

        reservationRepository.save(reservation);
    }

    private ReservationEvent toEvent(ReservationEventType type, Reservation reservation) {
        Seat seat = reservation.getSeat();
        Flight flight = seat.getFlight();

        return new ReservationEvent(
            type,
            reservation.getUser().getEmail(),
            reservation.getUser().getName(),
            flight.getFlightNumber(),
            flight.getDepartureAirport(),
            flight.getArrivalAirport(),
            DEPARTURE_TIME_FORMATTER.format(flight.getDepartureTime()),
            seat.getSeatNumber()
        );
    }

    private void publishAfterCommit(String routingKey, ReservationEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                amqpTemplate.convertAndSend(RabbitConfig.RESERVATION_EXCHANGE, routingKey, event);
            }
        });
    }
}
