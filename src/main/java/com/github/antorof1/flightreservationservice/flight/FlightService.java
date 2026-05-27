package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {
    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public Flight createFlight(Flight flight) {
        if (flightRepository.existsByFlightNumber(flight.getFlightNumber())) {
            throw new IllegalArgumentException("Flight number is already in use: " + flight.getFlightNumber());
        }

        return flightRepository.save(flight);
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
}
