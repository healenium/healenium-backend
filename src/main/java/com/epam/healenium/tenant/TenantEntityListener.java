package com.epam.healenium.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Sets {@code tenant_id} on insert.
 * <ul>
 *   <li>Pro: requires {@link TenantContext} (or pre-set tenantId on entity).</li>
 *   <li>Free: falls back to {@link #FREE_TENANT_ID} when context is absent.</li>
 * </ul>
 */
public class TenantEntityListener {

    public static final UUID FREE_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof TenantAwareEntity tenantAware)) {
            return;
        }
        if (tenantAware.getTenantId() != null) {
            return;
        }

        UUID contextTenantId = TenantContext.getTenantId();
        if (contextTenantId != null) {
            tenantAware.setTenantId(contextTenantId);
            return;
        }

        if (TenantEdition.isPro()) {
            throw new IllegalStateException(
                    "Tenant is not set in TenantContext for entity: " + entity.getClass().getName());
        }
        tenantAware.setTenantId(FREE_TENANT_ID);
    }
}
