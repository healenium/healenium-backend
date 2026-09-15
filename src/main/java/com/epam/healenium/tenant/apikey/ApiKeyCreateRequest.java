package com.epam.healenium.tenant.apikey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ApiKeyCreateRequest {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String name;
}
