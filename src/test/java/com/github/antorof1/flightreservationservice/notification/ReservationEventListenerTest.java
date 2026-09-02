package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.factory.ReservationEventTestDataFactory;
import com.github.antorof1.flightreservationservice.reservation.event.ReservationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationEventListenerTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private ReservationEmailComposer composer;

    @InjectMocks
    private ReservationEventListener listener;

    @Test
    @DisplayName("Should hand the composed message to the notification service")
    void onReservationEvent_ComposesAndSends() {
        ReservationEvent event = ReservationEventTestDataFactory.confirmedEvent();
        EmailMessage message = new EmailMessage(event.passengerEmail(), "subject", "text");

        when(composer.compose(event)).thenReturn(message);

        listener.onReservationEvent(event);

        verify(composer).compose(event);
        verify(notificationService).sendEmail(message);
    }

    @Test
    @DisplayName("Should propagate a composition failure so the message is not acknowledged as sent")
    void onReservationEvent_CompositionFails_Propagates() {
        ReservationEvent event = ReservationEventTestDataFactory.cancelledEvent();

        when(composer.compose(event)).thenThrow(new IllegalArgumentException("unsupported event"));

        assertThatThrownBy(() -> listener.onReservationEvent(event))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(notificationService);
    }
}
