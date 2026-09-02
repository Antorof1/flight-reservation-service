package com.github.antorof1.flightreservationservice.config;

import com.github.antorof1.flightreservationservice.notification.DeliveryMode;
import com.github.antorof1.flightreservationservice.notification.NotificationProperties;
import com.github.antorof1.flightreservationservice.notification.ResendProperties;
import com.resend.Resend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class ResendConfig {
    @Bean
    public Resend resend(ResendProperties properties, NotificationProperties notifications) {
        if (notifications.mode() != DeliveryMode.OFF) {
            Assert.hasText(properties.apiKey(), () -> missing("RESEND_API_KEY", notifications.mode()));
            Assert.hasText(properties.fromEmail(), () -> missing("RESEND_FROM_EMAIL", notifications.mode()));
        }

        return new Resend(properties.apiKey());
    }

    private static String missing(String variable, DeliveryMode mode) {
        return "%s is required when app.notifications.mode is %s".formatted(variable, mode);
    }
}
