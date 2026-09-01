package com.github.antorof1.flightreservationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String RESERVATION_EXCHANGE = "reservation.events";
    public static final String RESERVATION_CONFIRMED_KEY = "reservation.confirmed";
    public static final String RESERVATION_CANCELLED_KEY = "reservation.cancelled";
    public static final String RESERVATION_BINDING_PATTERN = "reservation.*";
    public static final String EMAIL_QUEUE = "email-notification.reservation-event";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange reservationExchange() {
        return ExchangeBuilder.topicExchange(RESERVATION_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public Binding emailNotificationBinding(Queue emailNotificationQueue, TopicExchange reservationExchange) {
        return BindingBuilder
            .bind(emailNotificationQueue)
            .to(reservationExchange)
            .with(RESERVATION_BINDING_PATTERN);
    }
}
