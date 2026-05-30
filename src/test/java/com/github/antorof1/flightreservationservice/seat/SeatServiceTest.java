package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.flight.FlightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {
    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightService flightService;

    @InjectMocks
    private SeatService seatService;

    @Test
    @DisplayName("Should return all seats for a flight when no status is provided")
    void getSeatsByFlightId_NoStatus_ReturnsAllSeats() {
        Long flightId = 1L;
        when(flightService.existsById(flightId)).thenReturn(true);
        List<Seat> expectedSeats = List.of(
            new Seat(new Flight(), "A1", SeatClass.ECONOMY, BigDecimal.valueOf(100), SeatStatus.AVAILABLE),
            new Seat(new Flight(), "A2", SeatClass.ECONOMY, BigDecimal.valueOf(100), SeatStatus.BOOKED)
        );
        when(seatRepository.findByFlightId(flightId)).thenReturn(expectedSeats);

        List<Seat> actualSeats = seatService.getSeatsByFlightId(flightId, null);

        assertThat(actualSeats).hasSize(2);
        assertThat(actualSeats).isEqualTo(expectedSeats);
        verify(flightService).existsById(flightId);
        verify(seatRepository).findByFlightId(flightId);
    }

    @Test
    @DisplayName("Should return seats for a flight filtered by status")
    void getSeatsByFlightId_WithStatus_ReturnsFilteredSeats() {
        Long flightId = 1L;
        SeatStatus status = SeatStatus.AVAILABLE;
        when(flightService.existsById(flightId)).thenReturn(true);
        List<Seat> expectedSeats = List.of(
            new Seat(new Flight(), "A1", SeatClass.ECONOMY, BigDecimal.valueOf(100), status)
        );
        when(seatRepository.findByFlightIdAndStatus(flightId, status)).thenReturn(expectedSeats);

        List<Seat> actualSeats = seatService.getSeatsByFlightId(flightId, status);

        assertThat(actualSeats).hasSize(1);
        assertThat(actualSeats.getFirst().getStatus()).isEqualTo(status);
        verify(flightService).existsById(flightId);
        verify(seatRepository).findByFlightIdAndStatus(flightId, status);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when flight does not exist")
    void getSeatsByFlightId_FlightNotFound_ThrowsException() {
        Long flightId = 1L;
        when(flightService.existsById(flightId)).thenReturn(false);

        assertThatThrownBy(() -> seatService.getSeatsByFlightId(flightId, null))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Flight not found");

        verify(flightService).existsById(flightId);
        verify(seatRepository, never()).findByFlightId(any());
    }

    @Test
    @DisplayName("Should return seat by id when it exists")
    void getSeatById_Exists_ReturnsSeat() {
        Long seatId = 1L;
        Seat seat = new Seat(new Flight(), "A1", SeatClass.ECONOMY, BigDecimal.valueOf(100), SeatStatus.AVAILABLE);
        seat.setId(seatId);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        Seat actualSeat = seatService.getSeatById(seatId);

        assertThat(actualSeat).isEqualTo(seat);
        verify(seatRepository).findById(seatId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when seat id does not exist")
    void getSeatById_NotExists_ThrowsException() {
        Long seatId = 1L;
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatService.getSeatById(seatId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Seat not found with id: " + seatId);

        verify(seatRepository).findById(seatId);
    }

    @Test
    @DisplayName("Should update seat status and return updated seat")
    void updateSeatStatus_Success() {
        Long seatId = 1L;
        SeatStatus newStatus = SeatStatus.BOOKED;
        Seat seat = new Seat(new Flight(), "A1", SeatClass.ECONOMY, BigDecimal.valueOf(100), SeatStatus.AVAILABLE);
        seat.setId(seatId);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Seat updatedSeat = seatService.updateSeatStatus(seatId, newStatus);

        assertThat(updatedSeat.getStatus()).isEqualTo(newStatus);
        verify(seatRepository).findById(seatId);
        verify(seatRepository).save(seat);
    }
}
