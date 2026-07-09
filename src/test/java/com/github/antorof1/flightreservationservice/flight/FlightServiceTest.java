package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.exception.DuplicateResourceException;
import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatRepository;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {
    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatRepository seatRepository;

    @Captor
    private ArgumentCaptor<List<Seat>> seatsCaptor;

    @InjectMocks
    private FlightService flightService;

    @Test
    @DisplayName("Should successfully create a flight and generate seats")
    void createFlight_Success() {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(6));
        when(flightRepository.existsByFlightNumber(flight.getFlightNumber())).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight createdFlight = flightService.createFlight(flight);

        assertThat(createdFlight).isEqualTo(flight);
        verify(flightRepository).existsByFlightNumber(flight.getFlightNumber());
        verify(flightRepository).save(flight);

        verify(seatRepository).saveAll(seatsCaptor.capture());

        List<Seat> generatedSeats = seatsCaptor.getValue();
        assertThat(generatedSeats).hasSize(176);

        long firstClassCount = generatedSeats.stream().filter(s -> s.getSeatClass() == SeatClass.FIRST).count();
        long businessClassCount = generatedSeats.stream().filter(s -> s.getSeatClass() == SeatClass.BUSINESS).count();
        long economyClassCount = generatedSeats.stream().filter(s -> s.getSeatClass() == SeatClass.ECONOMY).count();

        assertThat(firstClassCount).isEqualTo(8);
        assertThat(businessClassCount).isEqualTo(24);
        assertThat(economyClassCount).isEqualTo(144);

        assertThat(generatedSeats).allSatisfy(seat -> {
            assertThat(seat.getFlight()).isEqualTo(flight);
            assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        });
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when flight number already exists")
    void createFlight_DuplicateFlightNumber_ThrowsException() {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(6));
        when(flightRepository.existsByFlightNumber(flight.getFlightNumber())).thenReturn(true);

        assertThatThrownBy(() -> flightService.createFlight(flight))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Flight number is already in use");

        verify(flightRepository).existsByFlightNumber(flight.getFlightNumber());
        verify(flightRepository, never()).save(any());
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should return a page of flights")
    void getAllFlights_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Flight> flights = List.of(new Flight(), new Flight());

        Page<Flight> flightPage = new PageImpl<>(flights, pageable, flights.size());

        when(flightRepository.findAll(pageable)).thenReturn(flightPage);

        Page<Flight> result = flightService.getAllFlights(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);

        verify(flightRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return flight by id when it exists")
    void getFlightById_Exists_ReturnsFlight() {
        Long flightId = 1L;
        Flight flight = new Flight();
        flight.setId(flightId);
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));

        Flight result = flightService.getFlightById(flightId);

        assertThat(result.getId()).isEqualTo(flightId);
        verify(flightRepository).findById(flightId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when flight id does not exist")
    void getFlightById_NotExists_ThrowsException() {
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(flightId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Flight not found with id: " + flightId);

        verify(flightRepository).findById(flightId);
    }

    @Test
    @DisplayName("Should return true when flight exists by id")
    void existsById_True() {
        Long flightId = 1L;
        when(flightRepository.existsById(flightId)).thenReturn(true);

        boolean exists = flightService.existsById(flightId);

        assertThat(exists).isTrue();
        verify(flightRepository).existsById(flightId);
    }

    @Test
    @DisplayName("Should return false when flight does not exist by id")
    void existsById_False() {
        Long flightId = 1L;
        when(flightRepository.existsById(flightId)).thenReturn(false);

        boolean exists = flightService.existsById(flightId);

        assertThat(exists).isFalse();
        verify(flightRepository).existsById(flightId);
    }
}
