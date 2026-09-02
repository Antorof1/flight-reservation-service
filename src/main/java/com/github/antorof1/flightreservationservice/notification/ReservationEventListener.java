package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.config.RabbitConfig;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {
    private final NotificationService notificationService;
    private final ReservationEmailComposer composer;

    public ReservationEventListener(NotificationService notificationService, ReservationEmailComposer composer) {
        this.notificationService = notificationService;
        this.composer = composer;
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void onReservationEvent(ReservationEvent event) {
        notificationService.sendEmail(composer.compose(event));
    }
}
