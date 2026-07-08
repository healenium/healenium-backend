package com.epam.healenium.tenant;

import java.util.UUID;

/**
 * Stores current tenant id for the duration of the request.
 *
 * NOTE: Must be cleared after each request to avoid cross-request leakage.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
