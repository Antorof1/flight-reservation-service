package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findAllByUser(User user, Pageable pageable);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, OffsetDateTime time);
}
