package com.epam.healenium.service;

import com.epam.healenium.model.dto.TenantSettingsDto;

import java.util.Map;

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

    /**
     * Update a single setting by key (legacy {@code POST /healenium/settings/update}).
     * Tenant settings follow {@link #updateSettings}; LOG_LEVEL is service-wide.
     *
     * @return map with key, value, success and an optional message
     */
    Map<String, Object> updateSetting(String key, String value);

    /**
     * Key selector URL flag of the current tenant, or {@code app.selector.key.url-for-key} when the tenant has none.
     */
    boolean isKeySelectorUrl();

    /**
     * Collect metrics flag of the current tenant, or {@code app.metrics.allow} when the tenant has none.
     */
    boolean isCollectMetrics();

    /**
     * findElements auto-healing flag of the current tenant, or {@code app.healing.elements} when the tenant has none.
     */
    boolean isFindElementsAutoHealing();

    default boolean isHealEnabled() {
        TenantSettingsDto settings = getSettings();
        return settings == null || !Boolean.FALSE.equals(settings.getHealEnabled());
    }

    /**
     * Recovery tries of the current tenant, or {@code healenium.recovery-tries} when the tenant has none.
     */
    int getRecoveryTries();

    /**
     * Score cap of the current tenant, or {@code healenium.score-cap} when the tenant has none.
     */
    double getScoreCap();

    /**
     * Healed type ({@code css} / {@code xpath}) of the current tenant, or {@code healenium.selector-type}
     * when the tenant has none.
     */
    String getHealedType();
}
