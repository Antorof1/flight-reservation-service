package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUser(User user);
}
