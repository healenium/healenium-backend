package com.epam.healenium.tenant.membership;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Resolves allowed tenant ids for a JWT identity ({@code iss} + {@code sub}) using {@code membership} rows.
 */
@Service
@Profile("pro")
public class MembershipResolutionService {

    private final MembershipRepository membershipRepository;

    private final Cache<CacheKey, List<UUID>> cache;

    public MembershipResolutionService(MembershipRepository membershipRepository,
                                       @Value("${healenium.membership.cache-ttl:PT5M}") Duration ttl,
                                       @Value("${healenium.membership.cache-max-size:10000}") long maxSize) {
        this.membershipRepository = membershipRepository;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    /**
     * Cached list of tenant ids (possibly empty if the user has no membership rows).
     */
    public List<UUID> resolveTenantIds(String issuer, String externalSub) {
        var key = new CacheKey(issuer, externalSub);
        return cache.get(key, k -> membershipRepository.findByIssuerAndExternalSub(k.issuer(), k.externalSub()).stream()
                .map(Membership::getTenantId)
                .toList());
    }

    private record CacheKey(String issuer, String externalSub) {
    }
}
