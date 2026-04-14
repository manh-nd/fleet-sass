package com.fleet.infrastructure.adapter.out.keycloak;

import com.fleet.infrastructure.config.FleetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Client to interact with Keycloak's UserInfo endpoint.
 * Fetches the full set of roles/permissions associated with an access token.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserInfoClient {

    private final RestTemplate restTemplate;
    private final FleetProperties properties;

    /**
     * Fetches user roles from the Keycloak UserInfo endpoint.
     *
     * @param accessToken The Bearer token issued by Keycloak
     * @return List of client roles for the fleet-notification-hub client
     */
    @SuppressWarnings("unchecked")
    public List<String> fetchUserRoles(String accessToken) {
        String userInfoUrl = properties.getKeycloak().getServerUrl() + "/realms/" + properties.getKeycloak().getRealm() + "/protocol/openid-connect/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            ).getBody();

            if (response == null || !response.containsKey("resource_access")) {
                return Collections.emptyList();
            }

            Map<String, Object> resourceAccess = (Map<String, Object>) response.get("resource_access");
            String clientId = properties.getKeycloak().getAdminClientId();

            if (!resourceAccess.containsKey(clientId)) {
                return Collections.emptyList();
            }

            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            return (List<String>) clientAccess.getOrDefault("roles", Collections.emptyList());

        } catch (Exception e) {
            log.error("Failed to fetch user info from Keycloak: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
