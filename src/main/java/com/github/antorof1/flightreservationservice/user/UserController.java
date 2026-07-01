package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
