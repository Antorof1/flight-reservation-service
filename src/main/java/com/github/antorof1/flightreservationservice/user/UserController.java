package com.github.antorof1.flightreservationservice.user;

import com.github.antorof1.flightreservationservice.reservation.Reservation;
import com.github.antorof1.flightreservationservice.reservation.ReservationService;
import com.github.antorof1.flightreservationservice.reservation.dto.ReservationResponse;
import com.github.antorof1.flightreservationservice.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "User", description = "The User API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;
    private final ReservationService reservationService;

    public UserController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    @Operation(summary = "Get current user reservations", description = "Retrieves a paginated list of reservations " +
        "for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user reservations")
    public ResponseEntity<Page<ReservationResponse>> getReservationsByUserId(
        @AuthenticationPrincipal JwtPrincipal principal, @ParameterObject @PageableDefault(sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        User user = userService.getUserById(principal.id());
        Page<Reservation> reservations = reservationService.getAllReservationsByUser(user, pageable);

        Page<ReservationResponse> reservationResponses =
            reservations.map(ReservationResponse::fromEntity);

        return ResponseEntity.ok(reservationResponses);
    }
}
