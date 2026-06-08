package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.user.dto.CreateUserRequest;
import com.github.antorof1.flightreservationservice.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "User", description = "The User API")
public class UserController {
    private final UserService userService;
    private final ReservationService reservationService;

    public UserController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @ApiResponse(responseCode = "400", description = "Invalid email format")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> getUserByEmail(
        @Parameter(description = "Email address of the user")
        @RequestParam
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email) {
        User user = userService.getUserByEmail(email);
        UserResponse response = UserResponse.fromEntity(user);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details.")
    @ApiResponse(responseCode = "201", description = "User successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = request.toEntity();
        User savedUser = userService.createUser(user);

        UserResponse response = UserResponse.fromEntity(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);

        UserResponse response = UserResponse.fromEntity(user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reservations")
    @Operation(summary = "Get user reservations", description = "Retrieves a paginated list of reservations for a " +
        "specific user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user reservations")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Page<ReservationResponse>> getReservationsByUserId(
        @PathVariable Long id, @ParameterObject @PageableDefault(sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userService.getUserById(id);
        Page<Reservation> reservations = reservationService.getAllReservationsByUser(user, pageable);

        Page<ReservationResponse> reservationResponses =
            reservations.map(ReservationResponse::fromEntity);

        return ResponseEntity.ok(reservationResponses);
    }
}
