package com.epam.healenium.tenant.membership;

import com.epam.healenium.tenant.registry.Tenant;
import com.epam.healenium.tenant.registry.TenantRepository;
import com.epam.healenium.tenant.registry.TenantStatuses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Idempotent auto-provisioning: creates a tenant + membership on first login.
 * Safe to call on every bootstrap request — returns the existing tenant if already provisioned.
 */
@Slf4j
@Service
@Profile("pro")
@RequiredArgsConstructor
public class UserProvisioningService {

    private final MembershipResolutionService membershipResolutionService;
    private final MembershipRepository membershipRepository;
    private final TenantRepository tenantRepository;

    /**
     * Returns the first tenant for the user, creating one if none exists.
     * The new tenant name is derived from the {@code sub} claim (first 8 chars).
     */
    @Transactional
    public UUID provisionIfNeeded(String issuer, String sub) {
        List<UUID> existing = membershipResolutionService.resolveTenantIds(issuer, sub);
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }

        UUID tenantId = UUID.randomUUID();
        String tenantName = "tenant-" + sub.substring(0, Math.min(8, sub.length()));

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setName(tenantName);
        tenant.setStatus(TenantStatuses.ACTIVE);
        tenantRepository.save(tenant);

        Membership membership = new Membership();
        membership.setIssuer(issuer);
        membership.setExternalSub(sub);
        membership.setTenantId(tenantId);
        membershipRepository.save(membership);

        membershipResolutionService.invalidate(issuer, sub);

        log.info("Auto-provisioned tenant {} ({}) for sub={}", tenantId, tenantName, sub);
        return tenantId;
    }
}
