package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.flight.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    boolean existsByFlightAndSeatNumber(Flight flight, String seatNumber);
}
