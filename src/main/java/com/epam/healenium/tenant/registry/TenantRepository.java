package com.epam.healenium.tenant.registry;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@Profile("pro")
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsByIdAndStatus(UUID id, String status);
}
