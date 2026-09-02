package com.github.antorof1.flightreservationservice.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("resend")
public record ResendProperties(
    @DefaultValue("") String apiKey,
    @DefaultValue("") String fromEmail
) {
}
