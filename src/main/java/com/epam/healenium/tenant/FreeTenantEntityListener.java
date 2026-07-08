package com.epam.healenium.tenant;

import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Free edition listener: no multitenancy; avoids TenantContext requirement.
 *
 * IMPORTANT: Since tenant_id column exists in DB schema, it must still be populated.
 * We generate a deterministic per-instance tenant id based on selector uid/etc. is not possible.
 * Therefore we use a constant value to satisfy NOT NULL constraints.
 */
public class FreeTenantEntityListener {

    /**
     * Fixed tenant id for Free edition installations.
     * This value is NOT exposed to users and is used only to satisfy schema constraints.
     */
    public static final UUID FREE_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @PrePersist
    public void prePersist(Object entity) {
        if (!(entity instanceof TenantAwareEntity tenantAware)) {
            return;
        }

        UUID ctx = TenantContext.getTenantId();
        if (ctx != null) {
            return;
        }

        if (tenantAware.getTenantId() == null) {
            tenantAware.setTenantId(FREE_TENANT_ID);
        }
    }
}
