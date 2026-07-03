package com.github.antorof1.flightreservationservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI flightReservationOpenAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .info(new Info()
                .title("Flight Reservation Service API")
                .description("API for managing flights, seats, users, and reservations.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("API Support")
                    .url("https://github.com/antorof1/flight-reservation-service"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public OpenApiCustomizer securityResponsesCustomizer() {
        return openApi ->
            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                        ApiResponses responses = operation.getResponses();

                        if (!responses.containsKey("403")) {
                            responses.addApiResponse("403", new ApiResponse().description(
                                "You do not have permission to access this resource"
                            ));
                        }
                    }
                }));
    }
}
