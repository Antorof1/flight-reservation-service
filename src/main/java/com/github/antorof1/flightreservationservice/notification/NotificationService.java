package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.PermanentEmailException;
import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailSender emailSender;
    private final NotificationProperties properties;

    public NotificationService(EmailSender emailSender, NotificationProperties properties) {
        this.emailSender = emailSender;
        this.properties = properties;
    }

    public void sendEmail(EmailMessage message) {
        if (!properties.permits(message.to())) {
            log.info(
                "Suppressed email '{}' to {} (mode={})",
                message.subject(),
                message.to(),
                properties.mode()
            );
            return;
        }

        try {
            emailSender.send(message);
        } catch (PermanentEmailException e) {
            log.error("Permanently failed to send email '{}' to {}: {}",
                message.subject(), message.to(), e.getMessage(), e);
        } catch (TransientEmailException e) {
            log.error("Giving up on email '{}' to {} after {} retries: {}",
                message.subject(), message.to(), properties.retry().maxRetries(), e.getMessage(), e);
        }
    }
}
