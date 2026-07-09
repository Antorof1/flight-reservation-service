package com.github.antorof1.flightreservationservice.seat;

import com.github.antorof1.flightreservationservice.AbstractControllerTest;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.seat.dto.SeatStatusUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
class SeatControllerTest extends AbstractControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SeatService seatService;

    @Test
    @DisplayName("PATCH /api/v1/seats/{id}/status should update seat status")
    void shouldUpdateSeatStatus() throws Exception {
        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);

        Seat seat = new Seat(flight, "12A", SeatClass.ECONOMY, new BigDecimal("100.00"), SeatStatus.HELD);
        seat.setId(1L);

        SeatStatusUpdateRequest request = new SeatStatusUpdateRequest(SeatStatus.HELD);

        when(seatService.updateSeatStatus(1L, SeatStatus.HELD)).thenReturn(seat);

        mockMvc.perform(patch("/api/v1/seats/1/status")
                .with(authentication(mockAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("HELD"));
    }

    @Test
    @DisplayName("PATCH /api/v1/seats/{id}/status should return 403 when the caller is not an admin")
    void shouldReturn403WhenNonAdminUpdatesSeatStatus() throws Exception {
        SeatStatusUpdateRequest request = new SeatStatusUpdateRequest(SeatStatus.HELD);

        mockMvc.perform(patch("/api/v1/seats/1/status")
                .with(authentication(mockUserAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
