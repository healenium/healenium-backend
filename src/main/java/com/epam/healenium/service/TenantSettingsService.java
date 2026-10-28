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
}
