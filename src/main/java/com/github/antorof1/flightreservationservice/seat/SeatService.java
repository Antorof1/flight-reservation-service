package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.FlightService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SeatService {
    private final SeatRepository seatRepository;
    private final FlightService flightService;

    public SeatService(SeatRepository seatRepository, FlightService flightService) {
        this.seatRepository = seatRepository;
        this.flightService = flightService;
    }

    public List<Seat> getSeatsByFlightId(Long flightId, @Nullable SeatStatus status) {
        if (!flightService.existsById(flightId)) {
            throw new ResourceNotFoundException("Flight not found");
        }

        if (status != null) {
            return seatRepository.findByFlightIdAndStatus(flightId, status);
        }

        return seatRepository.findByFlightId(flightId);
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
    }

    @Transactional
    public Seat updateSeatStatus(Long id, SeatStatus status) {
        Seat seat = getSeatById(id);

        seat.setStatus(status);

        return seatRepository.save(seat);
    }
}
