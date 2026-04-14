package com.fleet.infrastructure.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestTemplate;

@Configuration
public class KeycloakAdminConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Keycloak keycloak(FleetProperties properties) {
        var keycloakProps = properties.getKeycloak();
        
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProps.getServerUrl())
                .realm(keycloakProps.getRealm())
                .clientId(keycloakProps.getAdminClientId())
                .clientSecret(keycloakProps.getAdminClientSecret())
                .grantType("client_credentials")
                .build();
    }
}
