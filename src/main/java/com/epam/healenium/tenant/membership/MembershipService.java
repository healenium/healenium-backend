package com.epam.healenium.tenant.membership;

import com.epam.healenium.tenant.registry.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Provisions Cognito (or other) identity → tenant bindings for the auth plane.
 */
@Service
@Profile("pro")
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final TenantRepository tenantRepository;
    private final MembershipResolutionService membershipResolutionService;

    @Transactional
    public Membership upsert(MembershipUpsertRequest request) {
        String issuer = request.getIssuer().trim();
        String externalSub = request.getExternalSub().trim();
        UUID tenantId = request.getTenantId();

        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant does not exist: " + tenantId);
        }

        Membership membership = membershipRepository
                .findByIssuerAndExternalSubAndTenantId(issuer, externalSub, tenantId)
                .orElseGet(Membership::new);

        membership.setIssuer(issuer);
        membership.setExternalSub(externalSub);
        membership.setTenantId(tenantId);
        if (StringUtils.hasText(request.getRole())) {
            membership.setRole(request.getRole().trim());
        }

        Membership saved = membershipRepository.save(membership);
        membershipResolutionService.invalidate(issuer, externalSub);
        return saved;
    }

    @Transactional
    public boolean delete(String issuer, String externalSub, UUID tenantId) {
        String trimmedIssuer = issuer.trim();
        String trimmedSub = externalSub.trim();
        int removed = membershipRepository.deleteByIssuerAndExternalSubAndTenantId(
                trimmedIssuer, trimmedSub, tenantId);
        if (removed > 0) {
            membershipResolutionService.invalidate(trimmedIssuer, trimmedSub);
            return true;
        }
        return false;
    }
}
