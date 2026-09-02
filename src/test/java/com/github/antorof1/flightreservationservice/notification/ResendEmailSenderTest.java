package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.PermanentEmailException;
import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendEmailSenderTest {
    private static final String FROM_EMAIL = "no-reply@test.example";
    private static final EmailMessage MESSAGE =
        new EmailMessage("john.doe@example.com", "Your flight FL-123 is confirmed", "See you on board");

    @Mock
    private Resend resend;

    @Mock
    private Emails emails;

    private ResendEmailSender emailSender;

    private static ResendException resendError(int statusCode, String errorName) {
        return new ResendException(statusCode, """
            {"name": "%s", "message": "rejected"}""".formatted(errorName));
    }

    @BeforeEach
    void setUp() {
        when(resend.emails()).thenReturn(emails);

        emailSender = new ResendEmailSender(resend, new ResendProperties("test-api-key", FROM_EMAIL));
    }

    @Test
    @DisplayName("Should send the message through Resend with the configured sender address")
    void send_Success() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(new CreateEmailResponse("email-id"));

        emailSender.send(MESSAGE);

        ArgumentCaptor<CreateEmailOptions> captor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        verify(emails).send(captor.capture());

        CreateEmailOptions options = captor.getValue();
        assertThat(options.getFrom()).isEqualTo(FROM_EMAIL);
        assertThat(options.getTo()).containsExactly(MESSAGE.to());
        assertThat(options.getSubject()).isEqualTo(MESSAGE.subject());
        assertThat(options.getText()).isEqualTo(MESSAGE.text());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "rate_limit_exceeded",
        "concurrent_idempotent_requests",
        "application_error",
        "service_unavailable"
    })
    @DisplayName("Should classify a retryable Resend error as transient")
    void send_TransientErrorName_ThrowsTransient(String errorName) throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(resendError(400, errorName));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(TransientEmailException.class)
            .hasMessageContaining(MESSAGE.to())
            .hasMessageContaining(errorName)
            .hasCauseInstanceOf(ResendException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"daily_quota_exceeded", "monthly_quota_exceeded"})
    @DisplayName("Should classify a quota error as permanent even when the status code looks retryable")
    void send_PermanentErrorName_ThrowsPermanent(String errorName) throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(resendError(429, errorName));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(PermanentEmailException.class)
            .hasMessageContaining(errorName);
    }

    @ParameterizedTest
    @CsvSource({
        "500, true",
        "503, true",
        "429, true",
        "422, false",
        "400, false",
        "401, false"
    })
    @DisplayName("Should fall back to the status code for an unrecognised Resend error")
    void send_UnknownErrorName_ClassifiedByStatusCode(int statusCode, boolean transientFailure) throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(resendError(statusCode, "something_unexpected"));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(transientFailure ? TransientEmailException.class : PermanentEmailException.class);
    }

    @Test
    @DisplayName("Should classify by status code when Resend returns no error name")
    void send_NoErrorName_ClassifiedByStatusCode() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class)))
            .thenThrow(new ResendException(503, "gateway is down"));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(TransientEmailException.class);
    }

    @Test
    @DisplayName("Should treat a Resend error without a status code as transient")
    void send_NoStatusCode_ThrowsTransient() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("rejected"));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(TransientEmailException.class);
    }

    @Test
    @DisplayName("Should treat a network failure as transient")
    void send_NetworkFailure_ThrowsTransient() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class)))
            .thenThrow(new RuntimeException("connection reset", new IOException("connection reset")));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(TransientEmailException.class)
            .hasMessageContaining(MESSAGE.to());
    }

    @Test
    @DisplayName("Should rethrow an unexpected runtime failure unchanged")
    void send_UnexpectedRuntimeFailure_Rethrown() throws ResendException {
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");
    }
}
