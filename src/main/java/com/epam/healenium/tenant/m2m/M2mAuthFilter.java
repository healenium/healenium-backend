package com.epam.healenium.tenant.m2m;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Machine-to-machine gate for Pro backend callers.
 * Registered only under {@code @Profile("pro")} — always required (no enforce toggle).
 *
 * <p>Accepts either {@link #INTERNAL_TOKEN_HEADER} (hlm-proxy / healenium-ai) or
 * {@link #API_KEY_HEADER} / Bearer key (Mobitru, direct CI).
 */
public class M2mAuthFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "Healenium-Api-Key";
    public static final String INTERNAL_TOKEN_HEADER = "Healenium-Internal-Token";

    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator",
            "/swagger",
            "/v3/api-docs"
    );

    private final M2mAuthProperties properties;

    public M2mAuthFilter(M2mAuthProperties properties) {
        this.properties = properties;
    }

    private static boolean skipPath(String path) {
        return SKIP_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return skipPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isAuthorized(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "M2M authentication required");
    }

    private boolean isAuthorized(HttpServletRequest request) {
        if (matchesInternalToken(request)) {
            return true;
        }
        return matchesApiKey(request);
    }

    private boolean matchesInternalToken(HttpServletRequest request) {
        String configured = properties.getInternalToken();
        if (!StringUtils.hasText(configured)) {
            return false;
        }
        return configured.equals(request.getHeader(INTERNAL_TOKEN_HEADER));
    }

    private boolean matchesApiKey(HttpServletRequest request) {
        if (properties.getApiKeys().isEmpty()) {
            return false;
        }
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(apiKey) && properties.getApiKeys().contains(apiKey)) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String bearer = authorization.substring("Bearer ".length()).trim();
            return properties.getApiKeys().contains(bearer);
        }
        return false;
    }
}
