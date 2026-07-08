package com.epam.healenium.tenant;

import com.epam.healenium.tenant.ai.TenantValidationService;
import com.epam.healenium.tenant.membership.MembershipResolutionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves tenant id from HTTP requests.
 *
 * <p>With a validated Bearer JWT: uses {@code membership} (issuer + sub) to determine allowed tenants.
 * Without JWT (e.g. WebDriver): requires {@value TENANT_HEADER} and validates it against healenium-ai.
 */
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantValidationService tenantValidationService;

    private final MembershipResolutionService membershipResolutionService;

    public TenantFilter(TenantValidationService tenantValidationService,
                        MembershipResolutionService membershipResolutionService) {
        this.tenantValidationService = tenantValidationService;
        this.membershipResolutionService = membershipResolutionService;
    }

    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator",
            "/swagger",
            "/v3/api-docs"
    );

    /** Paths that must not require {@value TENANT_HEADER} (JWT-only bootstrap). */
    private static boolean skipTenantHeader(String path) {
        return path.equals("/healenium/me") || path.startsWith("/healenium/me/");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (SKIP_PREFIXES.stream().anyMatch(path::startsWith)) {
            return true;
        }
        return skipTenantHeader(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String issuer = jwt.getClaimAsString("iss");
            String sub = jwt.getSubject();
            List<UUID> allowed = membershipResolutionService.resolveTenantIds(issuer, sub);
            if (allowed.isEmpty()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tenant membership for this user");
                return;
            }

            UUID effectiveTenantId;
            if (allowed.size() == 1) {
                effectiveTenantId = allowed.get(0);
                String rawHeader = request.getHeader(TENANT_HEADER);
                if (rawHeader != null && !rawHeader.isBlank()) {
                    UUID requested;
                    try {
                        requested = UUID.fromString(rawHeader.trim());
                    } catch (IllegalArgumentException e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, TENANT_HEADER + " must be a UUID");
                        return;
                    }
                    if (!effectiveTenantId.equals(requested)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                TENANT_HEADER + " does not match allowed tenant for this user");
                        return;
                    }
                }
            } else {
                String rawHeader = request.getHeader(TENANT_HEADER);
                if (rawHeader == null || rawHeader.isBlank()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            TENANT_HEADER + " header is required for multi-tenant users");
                    return;
                }
                UUID requested;
                try {
                    requested = UUID.fromString(rawHeader.trim());
                } catch (IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, TENANT_HEADER + " must be a UUID");
                    return;
                }
                if (!allowed.contains(requested)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            TENANT_HEADER + " is not an allowed tenant for this user");
                    return;
                }
                effectiveTenantId = requested;
            }

            if (!tenantValidationService.isTenantAllowed(effectiveTenantId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant is not active or does not exist");
                return;
            }
            continueWithTenant(request, response, filterChain, effectiveTenantId);
            return;
        }

        String rawTenantId = request.getHeader(TENANT_HEADER);
        if (rawTenantId == null || rawTenantId.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, TENANT_HEADER + " header is required");
            return;
        }

        UUID tenantId;
        try {
            tenantId = UUID.fromString(rawTenantId.trim());
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, TENANT_HEADER + " must be a UUID");
            return;
        }

        if (!tenantValidationService.isTenantAllowed(tenantId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant is not active or does not exist");
            return;
        }

        continueWithTenant(request, response, filterChain, tenantId);
    }

    private static void continueWithTenant(HttpServletRequest request,
                                           HttpServletResponse response,
                                           FilterChain filterChain,
                                           UUID tenantId) throws ServletException, IOException {
        try {
            TenantContext.setTenantId(tenantId);
            MDC.put("tenant_id", tenantId.toString());
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("tenant_id");
            TenantContext.clear();
        }
    }
}
