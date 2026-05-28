package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public Seat addSeat(Seat seat) {
        if (seatRepository.existsByFlightAndSeatNumber(seat.getFlight(), seat.getSeatNumber())) {
            throw new IllegalArgumentException(
                "Flight and seat number combination is already in use: " + seat.getFlight().getId() + " " +
                    seat.getSeatNumber());
        }

        return seatRepository.save(seat);
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
    }

    public Seat updateSeat(Long id, Seat seatDetails) {
        Seat seat = getSeatById(id);

        boolean flightChanged = !seat.getFlight().equals(seatDetails.getFlight());
        boolean seatNumberChanged = !seat.getSeatNumber().equals(seatDetails.getSeatNumber());

        if ((flightChanged || seatNumberChanged) &&
            seatRepository.existsByFlightAndSeatNumber(seatDetails.getFlight(), seatDetails.getSeatNumber())) {
            throw new IllegalArgumentException(
                "Flight and seat number combination is already in use: " + seatDetails.getFlight().getId() + " " +
                    seatDetails.getSeatNumber());
        }

        seat.setFlight(seatDetails.getFlight());
        seat.setSeatNumber(seatDetails.getSeatNumber());
        seat.setSeatClass(seatDetails.getSeatClass());
        seat.setPrice(seatDetails.getPrice());
        seat.setStatus(seatDetails.getStatus());

        return seatRepository.save(seat);
    }

    public void deleteSeat(Long id) {
        Seat seat = getSeatById(id);
        seatRepository.delete(seat);
    }
}
