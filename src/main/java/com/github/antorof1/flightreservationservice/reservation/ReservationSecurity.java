package com.github.antorof1.flightreservationservice.reservation;

import com.github.antorof1.flightreservationservice.security.JwtPrincipal;
import com.github.antorof1.flightreservationservice.user.UserRole;
import org.springframework.stereotype.Component;

@Component
public class ReservationSecurity {
    private final ReservationService reservationService;

    public ReservationSecurity(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public boolean canAccessReservation(JwtPrincipal principal, Long reservationId) {
        if (principal.role() == UserRole.ADMIN) {
            return true;
        }

        Reservation reservation = reservationService.getReservationById(reservationId);
        return reservation.getUser().getId().equals(principal.id());
    }
}
