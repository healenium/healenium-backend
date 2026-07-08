package com.epam.healenium.tenant.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Validates tenant id against healenium-ai and caches results.
 */
@Service
@Profile("pro")
public class TenantValidationService {

    private final TenantRegistryClient tenantRegistryClient;

    private final Cache<UUID, Boolean> cache;

    public TenantValidationService(TenantRegistryClient tenantRegistryClient,
                                  @Value("${healenium.ai.tenant-cache-ttl:PT5M}") Duration ttl,
                                  @Value("${healenium.ai.tenant-cache-max-size:10000}") long maxSize) {
        this.tenantRegistryClient = tenantRegistryClient;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    public boolean isTenantAllowed(UUID tenantId) {
        Boolean cached = cache.getIfPresent(tenantId);
        if (cached != null) {
            return cached;
        }
        boolean allowed = tenantRegistryClient.isTenantActive(tenantId);
        cache.put(tenantId, allowed);
        return allowed;
    }
}
