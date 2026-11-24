package com.epam.healenium.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves tenant id from {@value #TENANT_HEADER} header on Pro installations.
 * In Open Source Community / Single-tenant mode, falls back to {@link #DEFAULT_TENANT_ID}.
 */
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "Healenium-Tenant-Id";
    public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/actuator",
            "/swagger",
            "/v3/api-docs",
            "/health"
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

        UUID tenantId;
        if (StringUtils.hasText(rawTenantId)) {
            try {
                tenantId = UUID.fromString(rawTenantId.trim());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format in {} header: {}", TENANT_HEADER, rawTenantId);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Healenium-Tenant-Id UUID format");
                return;
            }
        } else {
            // Open Source / Community / Single-Tenant mode
            tenantId = DEFAULT_TENANT_ID;
        }

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
