package com.fleet.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom converter that enriches the JWT authentication with granular permissions
 * fetched from Keycloak UserInfo/Redis cache.
 */
@Component
@RequiredArgsConstructor
public class EnrichedJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final PermissionEnricher permissionEnricher;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userId = jwt.getSubject();
        
        // Extract tenant from the 'tenants' claim (which we kept in the JWT)
        List<String> tenants = jwt.getClaimAsStringList("tenants");
        String tenantId = (tenants != null && !tenants.isEmpty()) ? tenants.get(0) : "default";

        // Fetch enriched permissions (Cache-aside)
        List<String> granularRoles = permissionEnricher.getEnrichedPermissions(
                userId, 
                tenantId, 
                jwt.getTokenValue()
        );

        // Map to Spring Security authorities with ROLE_ prefix
        Collection<GrantedAuthority> authorities = granularRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new JwtAuthenticationToken(jwt, authorities);
    }
}
