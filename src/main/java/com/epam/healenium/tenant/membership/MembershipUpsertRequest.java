package com.epam.healenium.tenant.membership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MembershipUpsertRequest {

    @NotBlank
    private String issuer;

    @NotBlank
    private String externalSub;

    @NotNull
    private UUID tenantId;

    private String role;
}
