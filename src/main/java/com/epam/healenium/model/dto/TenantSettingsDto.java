package com.epam.healenium.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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

    // Backward-compatibility aliases for legacy clients
    @JsonProperty("KEY_SELECTOR_URL")
    public Boolean getKeySelectorUrlUpper() {
        return keySelectorUrl;
    }

    @JsonProperty("KEY_SELECTOR_URL")
    public void setKeySelectorUrlUpper(Boolean value) {
        if (value != null && this.keySelectorUrl == null) {
            this.keySelectorUrl = value;
        }
    }

    @JsonProperty("COLLECT_METRICS")
    public Boolean getCollectMetricsUpper() {
        return collectMetrics;
    }

    @JsonProperty("COLLECT_METRICS")
    public void setCollectMetricsUpper(Boolean value) {
        if (value != null && this.collectMetrics == null) {
            this.collectMetrics = value;
        }
    }

    @JsonProperty("FIND_ELEMENTS_AUTO_HEALING")
    public Boolean getFindElementsAutoHealingUpper() {
        return findElementsAutoHealing;
    }

    @JsonProperty("FIND_ELEMENTS_AUTO_HEALING")
    public void setFindElementsAutoHealingUpper(Boolean value) {
        if (value != null && this.findElementsAutoHealing == null) {
            this.findElementsAutoHealing = value;
        }
    }

    @JsonProperty("SCORE_CAP")
    public Double getScoreCapUpper() {
        return scoreCap;
    }

    @JsonProperty("SCORE_CAP")
    public void setScoreCapUpper(Double value) {
        if (value != null && this.scoreCap == null) {
            this.scoreCap = value;
        }
    }

    @JsonProperty("RECOVERY_TRIES")
    public Integer getRecoveryTriesUpper() {
        return recoveryTries;
    }

    @JsonProperty("RECOVERY_TRIES")
    public void setRecoveryTriesUpper(Integer value) {
        if (value != null && this.recoveryTries == null) {
            this.recoveryTries = value;
        }
    }

    @JsonProperty("SELECTOR_TYPE")
    public String getSelectorTypeUpper() {
        return healedType;
    }

    @JsonProperty("SELECTOR_TYPE")
    public void setSelectorTypeUpper(String value) {
        if (value != null && this.healedType == null) {
            this.healedType = value;
        }
    }

    @JsonProperty("LOG_LEVEL")
    public String getLogLevel() {
        return System.getProperty("HLM_LOG_LEVEL", "INFO");
    }
}
