package com.epam.healenium.tenant.registry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TenantSyncRequest {

    @NotNull
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String status;

    /** Optional — set by hlm-ai on Trial provisioning, Pro upgrade, and renewal. */
    private OffsetDateTime subscriptionExpiresAt;
}
