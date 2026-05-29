package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.exception.InvalidReservationStateException;
import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.User;
import com.github.antorof1.flightreservationservice.user.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private static final String SEAT_LOCK_PREFIX = "seat:lock:";
    private static final long HOLD_MINUTES = 10;

    private final ReservationRepository reservationRepository;
    private final SeatService seatService;
    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    public ReservationService(ReservationRepository reservationRepository, SeatService seatService,
                              UserService userService, StringRedisTemplate redisTemplate) {
        this.reservationRepository = reservationRepository;
        this.seatService = seatService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Reservation createReservation(Long userId, Long seatId) {
        String seatLockKey = SEAT_LOCK_PREFIX + seatId;

        UUID lockToken = UUID.randomUUID();

        Boolean isLocked = redisTemplate.opsForValue()
            .setIfAbsent(seatLockKey, lockToken.toString(), HOLD_MINUTES, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(isLocked)) {
            throw new RuntimeException("Seat is currently held by another user"); // TODO: Use custom exception
        }

        try {
            Seat seat = seatService.getSeatById(seatId);

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Requested seat is no longer available"); // TODO: Use custom exception
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

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getAllReservationsByUser(User user) {
        return reservationRepository.findAllByUser(user);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    @Transactional
    public Reservation updateReservation(Long id, Reservation reservationDetails) {
        Reservation reservation = getReservationById(id);

        reservation.setUser(reservationDetails.getUser());
        reservation.setSeat(reservationDetails.getSeat());
        reservation.setStatus(reservationDetails.getStatus());
        reservation.setCreatedAt(reservationDetails.getCreatedAt());
        reservation.setExpiresAt(reservationDetails.getExpiresAt());

        return reservationRepository.save(reservation);
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

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long id) {
        Reservation reservation = getReservationById(id);


        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationStateException("Reservation is already cancelled");
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

        return reservation;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservationRepository.delete(reservation);
    }
}
