package com.github.antorof1.flightreservationservice.flight;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.dto.CreateFlightRequest;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatService;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
class FlightControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlightService flightService;

    @MockitoBean
    private SeatService seatService;

    @Test
    @DisplayName("GET /api/v1/flights should return a paginated list of flights")
    void shouldReturnAllFlights() throws Exception {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);
        Page<Flight> flightPage = new PageImpl<>(List.of(flight));

        when(flightService.getAllFlights(any(Pageable.class))).thenReturn(flightPage);

        mockMvc.perform(get("/api/v1/flights")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "departureTime,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].flightNumber").value("FL123"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} should return a flight when it exists")
    void shouldReturnFlightById() throws Exception {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);

        when(flightService.getFlightById(1L)).thenReturn(flight);

        mockMvc.perform(get("/api/v1/flights/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.flightNumber").value("FL123"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} should return 404 when flight not found")
    void shouldReturn404WhenFlightNotFound() throws Exception {
        when(flightService.getFlightById(99L)).thenThrow(new ResourceNotFoundException("Flight not found"));

        mockMvc.perform(get("/api/v1/flights/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Flight not found"));
    }


    @Test
    @DisplayName("GET /api/v1/flights/{id}/seats should return seats for a flight")
    void shouldReturnSeatsByFlightId() throws Exception {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);

        Seat seat = new Seat(flight, "12A", SeatClass.ECONOMY, new BigDecimal("100.00"), SeatStatus.AVAILABLE);
        seat.setId(1L);

        when(seatService.getSeatsByFlightId(1L, null)).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/v1/flights/1/seats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].seatNumber").value("12A"));
    }

    @Test
    @DisplayName("POST /api/v1/flights should create a new flight")
    void shouldCreateFlight() throws Exception {
        CreateFlightRequest request = new CreateFlightRequest(
            "FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5)
        );
        Flight savedFlight = request.toEntity();
        savedFlight.setId(1L);

        when(flightService.createFlight(any(Flight.class))).thenReturn(savedFlight);

        mockMvc.perform(post("/api/v1/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.flightNumber").value("FL123"));
    }

    @Test
    @DisplayName("POST /api/v1/flights should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        CreateFlightRequest invalidRequest = new CreateFlightRequest(
            "", "", "", null, null
        );

        mockMvc.perform(post("/api/v1/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.details.length()").value(5));
    }
}
