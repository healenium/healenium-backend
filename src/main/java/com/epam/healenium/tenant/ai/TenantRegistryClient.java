package com.epam.healenium.tenant.ai;

import java.util.UUID;

/**
 * Client for tenant registry (healenium-ai).
 */
public interface TenantRegistryClient {

    /**
     * @return true if tenant exists and is ACTIVE.
     */
    boolean isTenantActive(UUID tenantId);
}
