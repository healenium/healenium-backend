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
 *
 * <p>All three statuses (TRIAL, PAID, DISABLED) are considered "allowed" — the tenant
 * passes the {@link TenantFilter}. DISABLED tenants are additionally flagged as read-only
 * via {@link #isReadOnly(UUID)}, which downstream components use to block write operations.
 */
@Service
@Profile("pro")
public class TenantValidationService {

    private final TenantRepository tenantRepository;
    private final Cache<UUID, Boolean> allowedCache;
    private final Cache<UUID, Boolean> readOnlyCache;

    public TenantValidationService(TenantRepository tenantRepository,
                                   @Value("${healenium.tenant.cache-ttl:PT10M}") Duration ttl,
                                   @Value("${healenium.tenant.cache-max-size:10000}") long maxSize) {
        this.tenantRepository = tenantRepository;
        this.allowedCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
        this.readOnlyCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    /** Returns {@code true} for any existing tenant regardless of status. */
    public boolean isTenantAllowed(UUID tenantId) {
        return Boolean.TRUE.equals(allowedCache.get(tenantId, this::loadExists));
    }

    /** Returns {@code true} when the tenant's subscription has expired (status == DISABLED). */
    public boolean isReadOnly(UUID tenantId) {
        return Boolean.TRUE.equals(readOnlyCache.get(tenantId, this::loadReadOnly));
    }

    public void invalidate(UUID tenantId) {
        allowedCache.invalidate(tenantId);
        readOnlyCache.invalidate(tenantId);
    }

    public void invalidateAll() {
        allowedCache.invalidateAll();
        readOnlyCache.invalidateAll();
    }

    private boolean loadExists(UUID tenantId) {
        return tenantRepository.existsById(tenantId);
    }

    private boolean loadReadOnly(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .map(t -> TenantStatuses.isReadOnly(t.getStatus()))
                .orElse(false);
    }
}
