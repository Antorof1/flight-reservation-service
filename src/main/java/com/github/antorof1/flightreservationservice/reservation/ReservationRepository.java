package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUser(User user);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, OffsetDateTime time);
}
