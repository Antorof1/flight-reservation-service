package com.github.antorof1.flightreservationservice.reservation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationSweeper {
    private final ReservationService reservationService;

    public ReservationSweeper(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void releaseExpiredReservations() {
        reservationService.cleanUpExpiredReservations();
    }
}
