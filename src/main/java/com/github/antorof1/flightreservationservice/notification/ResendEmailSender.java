package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.EmailDeliveryException;
import com.github.antorof1.flightreservationservice.notification.exception.PermanentEmailException;
import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class ResendEmailSender implements EmailSender {
    private final static Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private static final Set<String> TRANSIENT_ERRORS = Set.of(
        "rate_limit_exceeded",
        "concurrent_idempotent_requests",
        "application_error",
        "service_unavailable"
    );

    private static final Set<String> PERMANENT_ERRORS = Set.of(
        "daily_quota_exceeded",
        "monthly_quota_exceeded"
    );

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailSender(Resend resend, ResendProperties properties) {
        this.resend = resend;
        this.fromEmail = properties.fromEmail();
    }

    private static EmailDeliveryException classify(ResendException e, String to) {
        String detail = "Resend rejected the email to %s (status=%s, error=%s): %s"
            .formatted(to, e.getStatusCode(), e.getErrorName(), e.getMessage());

        return isTransient(e)
            ? new TransientEmailException(detail, e)
            : new PermanentEmailException(detail, e);
    }

    private static boolean isTransient(ResendException e) {
        String errorName = e.getErrorName();

        if (errorName != null) {
            if (PERMANENT_ERRORS.contains(errorName)) {
                return false;
            }

            if (TRANSIENT_ERRORS.contains(errorName)) {
                return true;
            }
        }

        Integer statusCode = e.getStatusCode();

        if (statusCode == null) {
            return true;
        }

        return statusCode >= 500 || statusCode == 429;
    }

    @Override
    public void send(EmailMessage message) {
        CreateEmailOptions params = CreateEmailOptions.builder()
            .from(fromEmail)
            .to(message.to())
            .subject(message.subject())
            .text(message.text())
            .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info(
                "Sent email to {} with subject '{}' (resend id: {})",
                message.to(),
                message.subject(),
                response.getId()
            );
        } catch (ResendException e) {
            throw classify(e, message.to());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw new TransientEmailException("Could not reach Resend while sending to " + message.to(), e);
            }
            throw e;
        }
    }
}
