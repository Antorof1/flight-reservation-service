package com.github.antorof1.flightreservationservice.config;

import com.github.antorof1.flightreservationservice.exception.ApiErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
    public OpenApiCustomizer errorResponsesCustomizer() {
        return openApi -> {
            @SuppressWarnings("rawtypes")
            Map<String, Schema> schemas = ModelConverters.getInstance().readAll(ApiErrorResponse.class);
            schemas.forEach(openApi.getComponents()::addSchemas);

            Content errorContent = new Content().addMediaType(
                "application/json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
            );

            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();

                    if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                        if (!responses.containsKey("403")) {
                            responses.addApiResponse("403", new ApiResponse()
                                .description("You do not have permission to access this resource")
                                .content(errorContent)
                            );
                        }
                    }

                    responses.forEach((statusCode, response) -> {
                        // Check if the status code is a 4xx or 5xx error
                        if (statusCode.matches("^[45]\\d{2}$")) {
                            response.setContent(errorContent);
                        }
                    });
                })
            );
        };
    }
}
