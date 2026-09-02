package com.github.antorof1.flightreservationservice.config;

import com.github.antorof1.flightreservationservice.notification.DeliveryMode;
import com.github.antorof1.flightreservationservice.notification.NotificationProperties;
import com.github.antorof1.flightreservationservice.notification.ResendProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResendConfigTest {
    private final ResendConfig config = new ResendConfig();

    private static NotificationProperties notifications(DeliveryMode mode) {
        return new NotificationProperties(mode, Set.of(), new NotificationProperties.Retry(2, Duration.ofSeconds(2)));
    }

    @Test
    @DisplayName("Should create the client without credentials when delivery is off")
    void resend_ModeOffWithoutCredentials_CreatesClient() {
        assertThat(config.resend(new ResendProperties("", ""), notifications(DeliveryMode.OFF))).isNotNull();
    }

    @Test
    @DisplayName("Should fail fast when delivery is on without credentials")
    void resend_DeliveringWithoutCredentials_Throws() {
        assertThatThrownBy(() -> config.resend(new ResendProperties("", ""), notifications(DeliveryMode.ALL)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("RESEND_API_KEY");
    }
}
