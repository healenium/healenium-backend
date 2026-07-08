package com.epam.healenium.tenant.membership;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps an external identity (e.g. Cognito {@code iss} + {@code sub}) to a tenant row.
 * Not {@code TenantAwareEntity}: no RLS on {@code membership}.
 */
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(
        name = "membership",
        uniqueConstraints = @UniqueConstraint(
                name = "membership_issuer_external_sub_key",
                columnNames = {"issuer", "external_sub"}
        )
)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String issuer;

    @Column(name = "external_sub", nullable = false, columnDefinition = "TEXT")
    private String externalSub;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(length = 128)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
