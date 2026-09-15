package com.epam.healenium.tenant.apikey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantApiKeyRepository extends JpaRepository<TenantApiKey, UUID> {

    Optional<TenantApiKey> findByKeyAndStatus(String key, String status);

    List<TenantApiKey> findAllByTenantId(UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
