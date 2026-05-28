package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatRepository;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightService {
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;

    public FlightService(FlightRepository flightRepository, SeatRepository seatRepository) {
        this.flightRepository = flightRepository;
        this.seatRepository = seatRepository;
    }

    public Flight createFlight(Flight flight) {
        if (flightRepository.existsByFlightNumber(flight.getFlightNumber())) {
            throw new IllegalArgumentException("Flight number is already in use: " + flight.getFlightNumber());
        }

        Flight savedFlight = flightRepository.save(flight);

        List<Seat> seats = generateSeats(flight);
        seatRepository.saveAll(seats);

        return savedFlight;
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    public Flight updateFlight(Long id, Flight flightDetails) {
        Flight flight = getFlightById(id);

        if (!flight.getFlightNumber()
            .equals(flightDetails.getFlightNumber()) &&
            flightRepository.existsByFlightNumber(flightDetails.getFlightNumber())) {
            throw new IllegalArgumentException("Flight number is already in use: " + flightDetails.getFlightNumber());
        }

        flight.setFlightNumber(flightDetails.getFlightNumber());
        flight.setDepartureAirport(flightDetails.getDepartureAirport());
        flight.setArrivalAirport(flightDetails.getArrivalAirport());
        flight.setDepartureTime(flightDetails.getDepartureTime());
        flight.setArrivalTime(flightDetails.getArrivalTime());

        return flightRepository.save(flight);
    }

    public void deleteFlight(Long id) {
        Flight flight = getFlightById(id);
        flightRepository.delete(flight);
    }

    private List<Seat> generateSeats(Flight flight) {
        List<Seat> seats = new ArrayList<>();

        String[] fourAbreastLayout = {"A", "C", "D", "F"};
        String[] sixAbreastLayout = {"A", "B", "C", "D", "E", "F"};

        for (int row = 1; row <= 2; row++) {
            for (String letter : fourAbreastLayout) {
                String seatNumber = row + letter;
                seats.add(new Seat(
                    flight,
                    seatNumber,
                    SeatClass.FIRST,
                    BigDecimal.valueOf(500.00),
                    SeatStatus.AVAILABLE
                ));
            }
        }

        for (int row = 3; row <= 6; row++) {
            for (String letter : sixAbreastLayout) {
                String seatNumber = row + letter;
                seats.add(new Seat(
                    flight,
                    seatNumber,
                    SeatClass.BUSINESS,
                    BigDecimal.valueOf(250.00),
                    SeatStatus.AVAILABLE
                ));
            }
        }

        for (int row = 7; row <= 30; row++) {
            for (String letter : sixAbreastLayout) {
                String seatNumber = row + letter;
                seats.add(new Seat(
                    flight,
                    seatNumber,
                    SeatClass.ECONOMY,
                    BigDecimal.valueOf(100.00),
                    SeatStatus.AVAILABLE
                ));
            }
        }

        return seats;
    }
}
