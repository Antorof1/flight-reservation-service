package com.github.antorof1.flightreservationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flightReservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Flight Reservation Service API")
                        .description("API for managing flights, seats, users, and reservations.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .url("https://github.com/antorof1/flight-reservation-service")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
