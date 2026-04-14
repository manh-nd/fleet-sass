package com.fleet.config;

import com.fleet.infrastructure.adapter.in.web.filter.ApiKeyAuthFilter;
import com.fleet.infrastructure.security.EnrichedJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "fleet.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final EnrichedJwtAuthenticationConverter enrichedJwtConverter;

    public SecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter, EnrichedJwtAuthenticationConverter enrichedJwtConverter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.enrichedJwtConverter = enrichedJwtConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // API key filter runs before Spring Security's JWT filter
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Infrastructure probes — no auth required
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info")
                        .permitAll()
                        // Swagger UI — allow in dev; restrict in prod via properties if needed
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        // All API endpoints require authentication (JWT or API key)
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(enrichedJwtConverter)))
                .build();
    }
}
