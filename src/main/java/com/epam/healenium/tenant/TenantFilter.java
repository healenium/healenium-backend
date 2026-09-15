package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves tenant id from {@value #TENANT_HEADER} on Pro installations.
 *
 * <p>Callers (hlm-proxy, Mobitru, WebDriver) must supply the header. JWT and membership
 * resolution for UI happen in hlm-proxy. ACTIVE check uses the local {@code tenants} table.
 */
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "Healenium-Tenant-Id";

    private final TenantValidationService tenantValidationService;

    public TenantFilter(TenantValidationService tenantValidationService) {
        this.tenantValidationService = tenantValidationService;
    }

    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator",
            "/swagger",
            "/v3/api-docs",
            "/internal"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant does not exist");
            return;
        }

        boolean readOnly = tenantValidationService.isReadOnly(tenantId);
        continueWithTenant(request, response, filterChain, tenantId, readOnly);
    }

    private static void continueWithTenant(HttpServletRequest request,
                                           HttpServletResponse response,
                                           FilterChain filterChain,
                                           UUID tenantId,
                                           boolean readOnly) throws ServletException, IOException {
        try {
            TenantContext.setTenantId(tenantId);
            TenantContext.setReadOnly(readOnly);
            MDC.put("tenant_id", tenantId.toString());
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("tenant_id");
            TenantContext.clear();
        }
    }
}
