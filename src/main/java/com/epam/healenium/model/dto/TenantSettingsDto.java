package com.epam.healenium.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantSettingsDto {

    private UUID tenantId;
    private Boolean healEnabled;
    private Integer recoveryTries;
    private Double scoreCap;
    private String healedType;
    private Boolean keySelectorUrl;
    private Boolean collectMetrics;
    private Boolean findElementsAutoHealing;
    private LocalDateTime updatedAt;
}
