package com.epam.healenium.tenant.membership;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@Profile("pro")
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findByIssuerAndExternalSub(String issuer, String externalSub);
}
