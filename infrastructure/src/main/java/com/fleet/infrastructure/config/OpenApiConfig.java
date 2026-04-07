package com.fleet.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 documentation configuration using SpringDoc.
 *
 * <p>Swagger UI is available at: {@code http://localhost:8080/swagger-ui.html}</p>
 * <p>OpenAPI JSON spec at: {@code http://localhost:8080/v3/api-docs}</p>
 *
 * <h3>Authentication</h3>
 * <p>Two schemes are supported:</p>
 * <ul>
 *   <li><b>BearerAuth</b> — Keycloak JWT ({@code Authorization: Bearer <token>})</li>
 *   <li><b>ApiKeyAuth</b> — service API key ({@code X-API-Key: <key>})</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetNotificationHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fleet Notification Hub API")
                        .description("""
                                Centralized notification hub for fleet management services.
                                
                                Services can register, configure and trigger notifications
                                through multiple channels (Email, SMS, Webhook, Push).
                                
                                Supports rule-based alerting and direct-send notifications
                                with i18n template rendering and full delivery tracking.
                                """)
                        .version("3.0.0")
                        .contact(new Contact()
                                .name("Fleet Engineering")
                                .email("platform@fleet.io"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.fleet.io/notifications").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak JWT — obtain via POST /realms/fleet/protocol/openid-connect/token"))
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Service API key for machine-to-machine authentication")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("BearerAuth")
                        .addList("ApiKeyAuth"));
    }
}
