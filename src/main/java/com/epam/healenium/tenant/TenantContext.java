package com.epam.healenium.tenant;

import java.util.UUID;

/**
 * Stores per-request tenant state: tenant id and subscription read-only flag.
 *
 * NOTE: Must be cleared after each request to avoid cross-request leakage.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> READ_ONLY = new ThreadLocal<>();

    private TenantContext() {
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    /** Returns true when the tenant's subscription is expired (status == DISABLED). */
    public static boolean isReadOnly() {
        return Boolean.TRUE.equals(READ_ONLY.get());
    }

    public static void setReadOnly(boolean readOnly) {
        READ_ONLY.set(readOnly);
    }

    public static void clear() {
        TENANT_ID.remove();
        READ_ONLY.remove();
    }
}
