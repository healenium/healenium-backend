package com.epam.healenium.tenant.registry;

import java.util.Locale;
import java.util.Set;

/**
 * Status values stored in {@code tenants.status}. Aligned with healenium-ai {@code TenantStatus}.
 *
 * <ul>
 *   <li>{@code TRIAL} — active trial subscription, full access.</li>
 *   <li>{@code PAID}  — active paid (Pro) subscription, full access.</li>
 *   <li>{@code DISABLED} — subscription expired or manually blocked; read-only access.</li>
 * </ul>
 */
public final class TenantStatuses {

    public static final String TRIAL    = "TRIAL";
    public static final String PAID     = "PAID";
    public static final String DISABLED = "DISABLED";

    private static final Set<String> ALLOWED = Set.of(TRIAL, PAID, DISABLED);

    private TenantStatuses() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidTenantStatusException("status is required");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED.contains(normalized)) {
            throw new InvalidTenantStatusException("Unsupported tenant status: " + raw
                    + " (allowed: " + ALLOWED + ")");
        }
        return normalized;
    }

    public static boolean isReadOnly(String status) {
        return DISABLED.equals(status);
    }
}
