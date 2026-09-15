package com.epam.healenium.tenant.apikey;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Profile("pro")
public class ApiKeyService {

    private final TenantApiKeyRepository repository;
    private final Cache<String, Optional<UUID>> cache;

    public ApiKeyService(TenantApiKeyRepository repository,
                         @Value("${healenium.apikey.cache-ttl:PT10M}") Duration ttl,
                         @Value("${healenium.apikey.cache-max-size:10000}") long maxSize) {
        this.repository = repository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    @Transactional
    public ApiKeyResponse create(UUID tenantId, String name) {
        TenantApiKey entity = new TenantApiKey()
                .setKey(UUID.randomUUID().toString())
                .setTenantId(tenantId)
                .setName(name);
        entity = repository.save(entity);
        log.info("Created API key id={} for tenantId={}", entity.getId(), tenantId);
        return ApiKeyResponse.fromWithKey(entity);
    }

    /**
     * Resolves the tenant UUID for a given API key string. Caffeine-cached.
     */
    public Optional<UUID> resolve(String key) {
        return cache.get(key, k ->
                repository.findByKeyAndStatus(k, ApiKeyStatuses.ACTIVE)
                        .map(TenantApiKey::getTenantId)
        );
    }

    public List<ApiKeyResponse> list(UUID tenantId) {
        return repository.findAllByTenantId(tenantId).stream()
                .map(ApiKeyResponse::fromWithKey)
                .toList();
    }

    @Transactional
    public boolean revoke(UUID id) {
        return repository.findById(id).map(entity -> {
            String oldKey = entity.getKey();
            entity.setStatus(ApiKeyStatuses.DISABLED);
            repository.save(entity);
            cache.invalidate(oldKey);
            log.info("Revoked API key id={} tenantId={}", id, entity.getTenantId());
            return true;
        }).orElse(false);
    }
}
