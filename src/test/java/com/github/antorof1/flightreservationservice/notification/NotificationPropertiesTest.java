package com.github.antorof1.flightreservationservice.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPropertiesTest {
    private NotificationProperties bind(Map<String, Object> properties) {
        return new Binder(new MapConfigurationPropertySource(properties))
            .bind("app.notifications", Bindable.of(NotificationProperties.class))
            .orElseThrow(() -> new AssertionError("properties did not bind"));
    }

    @Test
    @DisplayName("Should bind all notification properties")
    void bind_AllPropertiesPresent() {
        NotificationProperties properties = bind(Map.of(
            "app.notifications.mode", "ALLOWLIST",
            "app.notifications.allowlist", "first@example.com,second@example.com",
            "app.notifications.retry.max-retries", "5",
            "app.notifications.retry.delay", "3s"
        ));

        assertThat(properties.mode()).isEqualTo(DeliveryMode.ALLOWLIST);
        assertThat(properties.allowList()).containsExactlyInAnyOrder("first@example.com", "second@example.com");
        assertThat(properties.retry().maxRetries()).isEqualTo(5);
        assertThat(properties.retry().delay()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("Should lowercase allowlist entries")
    void bind_AllowlistIsLowercased() {
        NotificationProperties properties = bind(Map.of(
            "app.notifications.mode", "ALLOWLIST",
            "app.notifications.allowlist", "John.Doe@EXAMPLE.com"
        ));

        assertThat(properties.allowList()).containsExactly("john.doe@example.com");
    }

    @Test
    @DisplayName("Should expose an unmodifiable allowlist")
    void allowList_IsUnmodifiable() {
        NotificationProperties properties = new NotificationProperties(
            DeliveryMode.ALLOWLIST,
            Set.of("john.doe@example.com"),
            new NotificationProperties.Retry(2, Duration.ofSeconds(2))
        );

        assertThatThrownBy(() -> properties.allowList().add("intruder@example.com"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should fall back to safe defaults for the properties that are not configured")
    void bind_PropertiesMissing_UsesDefaults() {
        NotificationProperties properties = bind(Map.of("app.notifications.retry.max-retries", "5"));

        assertThat(properties.mode()).isEqualTo(DeliveryMode.OFF);
        assertThat(properties.allowList()).isEmpty();
        assertThat(properties.retry().maxRetries()).isEqualTo(5);
        assertThat(properties.retry().delay()).isEqualTo(Duration.ofSeconds(2));
    }
}
