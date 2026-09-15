package com.epam.healenium.tenant.apikey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * API key linked to a tenant. Multiple keys per tenant are allowed.
 * Not {@link com.epam.healenium.tenant.TenantAwareEntity} — no RLS on {@code api_keys}.
 */
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "api_keys")
public class TenantApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String key;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, length = 32)
    private String status = ApiKeyStatuses.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}
