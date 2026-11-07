package com.epam.healenium.service;

import com.epam.healenium.model.dto.TenantSettingsDto;

public interface TenantSettingsService {

    /**
     * Get settings for the current tenant or default values if not configured.
     *
     * @return settings DTO
     */
    TenantSettingsDto getSettings();

    /**
     * Update or create settings for the current tenant.
     *
     * @param request settings update request
     * @return updated settings DTO
     */
    TenantSettingsDto updateSettings(TenantSettingsDto request);

    default boolean isKeySelectorUrl() {
        TenantSettingsDto settings = getSettings();
        return settings != null && Boolean.TRUE.equals(settings.getKeySelectorUrl());
    }

    default boolean isCollectMetrics() {
        TenantSettingsDto settings = getSettings();
        return settings != null && Boolean.TRUE.equals(settings.getCollectMetrics());
    }

    default boolean isFindElementsAutoHealing() {
        TenantSettingsDto settings = getSettings();
        return settings != null && Boolean.TRUE.equals(settings.getFindElementsAutoHealing());
    }

    default boolean isHealEnabled() {
        TenantSettingsDto settings = getSettings();
        return settings == null || !Boolean.FALSE.equals(settings.getHealEnabled());
    }

    default int getRecoveryTries() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getRecoveryTries() != null && settings.getRecoveryTries() > 0
                ? settings.getRecoveryTries() : 1;
    }

    default double getScoreCap() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getScoreCap() != null && settings.getScoreCap() > 0
                ? settings.getScoreCap() : 0.60;
    }

    default String getHealedType() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getHealedType() != null ? settings.getHealedType() : "css";
    }
}
