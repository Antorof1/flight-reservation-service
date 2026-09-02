package com.github.antorof1.flightreservationservice.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties("app.notifications")
public record NotificationProperties(
    @DefaultValue("OFF") DeliveryMode mode,
    @DefaultValue Set<String> allowList,
    @DefaultValue Retry retry
) {
    public NotificationProperties {
        allowList = allowList.stream()
            .map(elem -> elem.toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    }

    public boolean permits(String recipient) {
        return switch (mode) {
            case OFF -> false;
            case ALL -> true;
            case ALLOWLIST -> allowList.contains(recipient.trim().toLowerCase(Locale.ROOT));
        };
    }

    public record Retry(
        @DefaultValue("2") int maxRetries,
        @DefaultValue("2s") Duration delay
    ) {
    }
}
