package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import org.springframework.context.annotation.Primary;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RetryingEmailSender implements EmailSender {
    private final ResendEmailSender delegate;

    public RetryingEmailSender(ResendEmailSender delegate) {
        this.delegate = delegate;
    }

    @Override
    @Retryable(
        includes = TransientEmailException.class,
        maxRetriesString = "${app.notifications.retry.max-retries}",
        delayString = "${app.notifications.retry.delay}"
    )
    public void send(EmailMessage message) {
        delegate.send(message);
    }
}
