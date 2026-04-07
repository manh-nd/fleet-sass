package com.fleet.infrastructure.adapter.in.web.filter;

import com.fleet.domain.entitlement.model.ApiKey;
import com.fleet.domain.entitlement.port.out.ApiKeyRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Servlet filter for {@code X-API-Key} header authentication.
 *
 * <p>Runs before the JWT resource server filter. If a valid API key is found,
 * sets a Spring Security authentication context so JWT validation is skipped
 * for that request.</p>
 *
 * <p>The incoming key is hashed with SHA-256 and looked up in the repository
 * (which should cache results in Redis to avoid DB calls per request).</p>
 *
 * <p>MDC context is populated with {@code tenantId} and {@code serviceId}
 * for structured log correlation.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyRepositoryPort apiKeyRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String rawKey = request.getHeader(API_KEY_HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            // No API key header — let JWT filter handle it
            filterChain.doFilter(request, response);
            return;
        }

        Optional<ApiKey> apiKey = apiKeyRepository.findByHash(hash(rawKey));

        if (apiKey.isPresent() && apiKey.get().isValid()) {
            ApiKey key = apiKey.get();

            // Populate MDC for log correlation
            MDC.put("tenantId",  key.getTenantId().value().toString());
            MDC.put("serviceId", key.getServiceId().value());

            try {
                // Authenticate with ROLE_SERVICE authority
                var auth = new UsernamePasswordAuthenticationToken(
                        key.getServiceId().value(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("API key authentication successful for service={}, tenant={}",
                        key.getServiceId().value(), key.getTenantId().value());
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove("tenantId");
                MDC.remove("serviceId");
            }
        } else {
            log.warn("Invalid or expired API key attempt from IP={}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"code":"INVALID_API_KEY","message":"The provided API key is invalid or expired"}
                    """);
        }
    }

    private String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
