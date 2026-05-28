package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.user.dto.CreateUserRequest;
import com.github.antorof1.flightreservationservice.user.dto.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private final UserService userService;
    private final ReservationService reservationService;

    public UserController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUserByEmail(
        @RequestParam
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email) {
        User user = userService.getUserByEmail(email);
        UserResponse response = UserResponse.fromEntity(user);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = request.toEntity();
        User savedUser = userService.createUser(user);

        UserResponse response = UserResponse.fromEntity(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);

        UserResponse response = UserResponse.fromEntity(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationsByUserId(@PathVariable Long id) {
        User user = userService.getUserById(id);
        List<Reservation> reservations = reservationService.getAllReservationsByUser(user);

        List<ReservationResponse> reservationResponses =
            reservations.stream().map(ReservationResponse::fromEntity).toList();

        return ResponseEntity.ok(reservationResponses);
    }
}
