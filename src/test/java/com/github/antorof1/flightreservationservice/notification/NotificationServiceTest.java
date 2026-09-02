package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.PermanentEmailException;
import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    private static final EmailMessage MESSAGE =
        new EmailMessage("john.doe@example.com", "Your flight FL-123 is confirmed", "See you on board");

    @Mock
    private EmailSender emailSender;

    private NotificationService notificationService(DeliveryMode mode, Set<String> allowList) {
        NotificationProperties properties = new NotificationProperties(
            mode,
            allowList,
            new NotificationProperties.Retry(2, Duration.ofMillis(10))
        );

        return new NotificationService(emailSender, properties);
    }

    @Test
    @DisplayName("Should not send anything when delivery mode is OFF")
    void sendEmail_ModeOff_Suppressed() {
        notificationService(DeliveryMode.OFF, Set.of(MESSAGE.to())).sendEmail(MESSAGE);

        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("Should send to any recipient when delivery mode is ALL")
    void sendEmail_ModeAll_Sends() {
        notificationService(DeliveryMode.ALL, Set.of()).sendEmail(MESSAGE);

        verify(emailSender).send(MESSAGE);
    }

    @Test
    @DisplayName("Should send to an allowlisted recipient when delivery mode is ALLOWLIST")
    void sendEmail_ModeAllowlist_RecipientListed_Sends() {
        notificationService(DeliveryMode.ALLOWLIST, Set.of("john.doe@example.com")).sendEmail(MESSAGE);

        verify(emailSender).send(MESSAGE);
    }

    @Test
    @DisplayName("Should match the allowlist ignoring case and surrounding whitespace")
    void sendEmail_ModeAllowlist_NormalizesRecipient_Sends() {
        EmailMessage message = new EmailMessage("  John.Doe@EXAMPLE.com  ", MESSAGE.subject(), MESSAGE.text());

        notificationService(DeliveryMode.ALLOWLIST, Set.of("JOHN.DOE@example.COM")).sendEmail(message);

        verify(emailSender).send(message);
    }

    @Test
    @DisplayName("Should suppress a recipient that is not on the allowlist")
    void sendEmail_ModeAllowlist_RecipientNotListed_Suppressed() {
        notificationService(DeliveryMode.ALLOWLIST, Set.of("someone.else@example.com")).sendEmail(MESSAGE);

        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("Should swallow a permanent delivery failure")
    void sendEmail_PermanentFailure_Swallowed() {
        doThrow(new PermanentEmailException("invalid recipient", new RuntimeException()))
            .when(emailSender).send(MESSAGE);

        notificationService(DeliveryMode.ALL, Set.of()).sendEmail(MESSAGE);

        verify(emailSender).send(MESSAGE);
    }

    @Test
    @DisplayName("Should swallow a transient delivery failure once retries are exhausted")
    void sendEmail_TransientFailure_Swallowed() {
        doThrow(new TransientEmailException("rate limited", new RuntimeException()))
            .when(emailSender).send(MESSAGE);

        notificationService(DeliveryMode.ALL, Set.of()).sendEmail(MESSAGE);

        verify(emailSender).send(MESSAGE);
    }

    @Test
    @DisplayName("Should propagate an unexpected failure so the broker can dead-letter the message")
    void sendEmail_UnexpectedFailure_Propagates() {
        NotificationService notificationService = notificationService(DeliveryMode.ALL, Set.of());

        doThrow(new IllegalStateException("boom")).when(emailSender).send(MESSAGE);

        assertThatThrownBy(() -> notificationService.sendEmail(MESSAGE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }
}
