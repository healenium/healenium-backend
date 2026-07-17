package com.epam.healenium.tenant;

import com.epam.healenium.tenant.registry.TenantRepository;
import com.epam.healenium.tenant.registry.TenantStatuses;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Validates tenant id against the local {@code tenants} table and caches results.
 */
@Service
@Profile("pro")
public class TenantValidationService {

    private final TenantRepository tenantRepository;
    private final Cache<UUID, Boolean> cache;

    public TenantValidationService(TenantRepository tenantRepository,
                                  @Value("${healenium.tenant.cache-ttl:PT10M}") Duration ttl,
                                  @Value("${healenium.tenant.cache-max-size:10000}") long maxSize) {
        this.tenantRepository = tenantRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    public boolean isTenantAllowed(UUID tenantId) {
        return Boolean.TRUE.equals(cache.get(tenantId, this::loadActive));
    }

    public void invalidate(UUID tenantId) {
        cache.invalidate(tenantId);
    }

    private boolean loadActive(UUID tenantId) {
        return tenantRepository.existsByIdAndStatus(tenantId, TenantStatuses.ACTIVE);
    }
}
