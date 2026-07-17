package com.epam.healenium.tenant.registry;

import java.util.Locale;
import java.util.Set;

/**
 * Status values stored in {@code tenants.status}. Aligned with healenium-ai {@code TenantStatus}.
 */
public final class TenantStatuses {

    public static final String ACTIVE = "ACTIVE";
    public static final String DISABLED = "DISABLED";

    private static final Set<String> ALLOWED = Set.of(ACTIVE, DISABLED);

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

    public static boolean isActive(String status) {
        return ACTIVE.equals(status);
    }
}
