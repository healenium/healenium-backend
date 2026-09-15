package com.epam.healenium.tenant.apikey;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKeyResponse {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String status;
    private OffsetDateTime createdAt;
    /** Only populated on creation — never returned in list/get responses. */
    private String key;

    static ApiKeyResponse from(TenantApiKey entity) {
        return new ApiKeyResponse()
                .setId(entity.getId())
                .setTenantId(entity.getTenantId())
                .setName(entity.getName())
                .setStatus(entity.getStatus())
                .setCreatedAt(entity.getCreatedAt());
    }

    static ApiKeyResponse fromWithKey(TenantApiKey entity) {
        return from(entity).setKey(entity.getKey());
    }
}
