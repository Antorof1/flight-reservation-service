package com.github.antorof1.flightreservationservice.notification;

import com.github.antorof1.flightreservationservice.notification.exception.PermanentEmailException;
import com.github.antorof1.flightreservationservice.notification.exception.TransientEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(RetryingEmailSenderTest.RetryTestConfiguration.class)
@TestPropertySource(properties = {
    "app.notifications.retry.max-retries=2",
    "app.notifications.retry.delay=10ms"
})
class RetryingEmailSenderTest {
    private static final EmailMessage MESSAGE =
        new EmailMessage("john.doe@example.com", "Your flight FL-123 is confirmed", "See you on board");
    @MockitoBean
    private ResendEmailSender delegate;
    @Autowired
    private EmailSender emailSender;

    @Test
    @DisplayName("Should not retry a successful send")
    void send_Success_SendsOnce() {
        emailSender.send(MESSAGE);

        verify(delegate).send(MESSAGE);
    }

    @Test
    @DisplayName("Should retry a transient failure up to the configured number of retries and then give up")
    void send_TransientFailure_RetriesThenPropagates() {
        doThrow(new TransientEmailException("rate limited", new RuntimeException()))
            .when(delegate).send(any(EmailMessage.class));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(TransientEmailException.class);

        verify(delegate, times(3)).send(MESSAGE);
    }

    @Test
    @DisplayName("Should stop retrying as soon as a send succeeds")
    void send_TransientFailureThenSuccess_StopsRetrying() {
        doThrow(new TransientEmailException("rate limited", new RuntimeException()))
            .doNothing()
            .when(delegate).send(any(EmailMessage.class));

        assertThatCode(() -> emailSender.send(MESSAGE)).doesNotThrowAnyException();

        verify(delegate, times(2)).send(MESSAGE);
    }

    @Test
    @DisplayName("Should not retry a permanent failure")
    void send_PermanentFailure_NotRetried() {
        doThrow(new PermanentEmailException("invalid recipient", new RuntimeException()))
            .when(delegate).send(any(EmailMessage.class));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(PermanentEmailException.class);

        verify(delegate).send(MESSAGE);
    }

    @Test
    @DisplayName("Should not retry an unexpected runtime failure")
    void send_UnexpectedFailure_NotRetried() {
        doThrow(new IllegalStateException("boom")).when(delegate).send(any(EmailMessage.class));

        assertThatThrownBy(() -> emailSender.send(MESSAGE))
            .isInstanceOf(IllegalStateException.class);

        verify(delegate).send(MESSAGE);
    }

    @Configuration
    @EnableResilientMethods(proxyTargetClass = true)
    static class RetryTestConfiguration {
        @Bean
        @Primary
        RetryingEmailSender retryingEmailSender(ResendEmailSender delegate) {
            return new RetryingEmailSender(delegate);
        }
    }
}
