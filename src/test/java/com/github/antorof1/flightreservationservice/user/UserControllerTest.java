package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.exception.ResourceNotFoundException;
import com.github.antorof1.flightreservationservice.flight.Flight;
import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.ReservationStatus;
import com.github.antorof1.flightreservationservice.seat.Seat;
import com.github.antorof1.flightreservationservice.seat.SeatClass;
import com.github.antorof1.flightreservationservice.seat.SeatStatus;
import com.github.antorof1.flightreservationservice.user.command.CreateUserCommand;
import com.github.antorof1.flightreservationservice.user.dto.CreateUserRequest;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("GET /api/v1/users?email=... should return a user when it exists")
    void shouldReturnUserByEmail() throws Exception {
        User user = new User(
            "john.doe@example.com",
            "John Doe",
            "password123",
            UserRole.USER
        );
        user.setId(1L);

        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/v1/users")
                .param("email", "john.doe@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/users?email=... should return 400 when email is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .param("email", "invalid-email"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users should create a new user")
    void shouldCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
            "john.doe@example.com",
            "John Doe",
            "password123"
        );
        User savedUser = request.toEntity();
        savedUser.setId(1L);

        when(userService.createUser(any(CreateUserCommand.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return a user when it exists")
    void shouldReturnUserById() throws Exception {
        User user = new User(
            "john.doe@example.com",
            "John Doe",
            "password123",
            UserRole.USER
        );
        user.setId(1L);

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id}/reservations should return user reservations")
    void shouldReturnUserReservations() throws Exception {
        User user = new User(
            "john.doe@example.com",
            "John Doe",
            "password123",
            UserRole.USER
        );
        user.setId(1L);

        Flight flight = new Flight("FL123", "JFK", "LAX", OffsetDateTime.now(), OffsetDateTime.now().plusHours(5));
        flight.setId(1L);

        Seat seat = new Seat(flight, "12A", SeatClass.ECONOMY, new BigDecimal("100.00"), SeatStatus.AVAILABLE);
        seat.setId(1L);

        Reservation reservation = new Reservation(user, seat, ReservationStatus.PENDING, UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now()
            .plusMinutes(15));
        reservation.setId(1L);

        Page<Reservation> reservationPage = new PageImpl<>(List.of(reservation));

        when(userService.getUserById(1L)).thenReturn(user);
        when(reservationService.getAllReservationsByUser(eq(user), any(Pageable.class)))
            .thenReturn(reservationPage);

        mockMvc.perform(get("/api/v1/users/1/reservations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].userId").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }
}
