package com.epam.healenium.tenant.registry;

import com.epam.healenium.tenant.TenantValidationService;
import com.epam.healenium.tenant.apikey.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Profile("pro")
@RequiredArgsConstructor
public class TenantSyncService {

    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;
    private final ApiKeyService apiKeyService;

    @Transactional
    public Tenant upsert(TenantSyncRequest request) {
        String status = TenantStatuses.normalize(request.getStatus());
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String name = request.getName().trim();

        boolean isNew = !tenantRepository.existsById(request.getId());
        Tenant tenant = tenantRepository.findById(request.getId()).orElseGet(Tenant::new);
        tenant.setId(request.getId());
        tenant.setName(name);
        tenant.setStatus(status);
        if (request.getSubscriptionExpiresAt() != null) {
            tenant.setSubscriptionExpiresAt(request.getSubscriptionExpiresAt());
        }
        Tenant saved = tenantRepository.save(tenant);
        tenantValidationService.invalidate(saved.getId());

        if (isNew) {
            apiKeyService.create(saved.getId(), "default");
            log.info("Created default API key for new tenant {}", saved.getId());
        }

        return saved;
    }

    @Transactional
    public List<Tenant> upsertAll(List<TenantSyncRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(this::upsert).toList();
    }
}
