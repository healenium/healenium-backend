package com.epam.healenium.tenant;

import java.util.UUID;

public interface TenantAwareEntity {

    UUID getTenantId();

    /**
     * Setter must return the entity type to be compatible with Lombok @Accessors(chain = true).
     */
    TenantAwareEntity setTenantId(UUID tenantId);
}
