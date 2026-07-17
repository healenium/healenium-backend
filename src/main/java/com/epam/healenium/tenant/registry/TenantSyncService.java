package com.epam.healenium.tenant.registry;

import com.epam.healenium.tenant.TenantValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Profile("pro")
@RequiredArgsConstructor
public class TenantSyncService {

    private final TenantRepository tenantRepository;
    private final TenantValidationService tenantValidationService;

    @Transactional
    public Tenant upsert(TenantSyncRequest request) {
        String status = TenantStatuses.normalize(request.getStatus());
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String name = request.getName().trim();

        Tenant tenant = tenantRepository.findById(request.getId()).orElseGet(Tenant::new);
        tenant.setId(request.getId());
        tenant.setName(name);
        tenant.setStatus(status);
        Tenant saved = tenantRepository.save(tenant);
        tenantValidationService.invalidate(saved.getId());
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
