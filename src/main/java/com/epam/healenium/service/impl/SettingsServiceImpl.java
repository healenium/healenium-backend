package com.epam.healenium.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.model.domain.Settings;
import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.repository.SettingsRepository;
import com.epam.healenium.service.SettingsService;
import com.epam.healenium.tenant.TenantContext;
import com.epam.healenium.tenant.TenantFilter;
import com.epam.healenium.tenant.TenantTxFacade;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private static final List<String> VALID_LOG_LEVELS = List.of("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
    private static final String HEALENIUM_LOGGER = "com.epam.healenium";

    private static final String LOG_LEVEL = "LOG_LEVEL";
    private static final String KEY_SELECTOR_URL = "KEY_SELECTOR_URL";
    private static final String COLLECT_METRICS = "COLLECT_METRICS";
    private static final String FIND_ELEMENTS_AUTO_HEALING = "FIND_ELEMENTS_AUTO_HEALING";
    private static final String SCORE_CAP = "SCORE_CAP";
    private static final String SELECTOR_TYPE = "SELECTOR_TYPE";
    private static final String HEALED_TYPE = "HEALED_TYPE";
    private static final String RECOVERY_TRIES = "RECOVERY_TRIES";

    private final SettingsRepository settingsRepository;
    private final DynamicSettings dynamicSettings;
    private final TenantTxFacade tenantTx;

    private final Cache<UUID, TenantSettingsDto> settingsCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(2))
            .build();

    /**
     * The default tenant (anonymous / single-tenant mode) has no database row: its settings are {@link DynamicSettings}.
     */
    @Override
    public TenantSettingsDto getSettings() {
        UUID tenantId = resolveTenantId();
        if (isDefaultTenant(tenantId)) {
            return buildDefaultDto(tenantId);
        }
        return settingsCache.get(tenantId, id -> tenantTx.required(() ->
                settingsRepository.findByTenantId(id)
                        .map(this::toDto)
                        .orElseGet(() -> buildDefaultDto(id))
        ));
    }

    @Override
    public boolean isKeySelectorUrl() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getKeySelectorUrl() != null
                ? settings.getKeySelectorUrl() : dynamicSettings.isKeySelectorUrl();
    }

    @Override
    public boolean isCollectMetrics() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getCollectMetrics() != null
                ? settings.getCollectMetrics() : dynamicSettings.isCollectMetrics();
    }

    @Override
    public boolean isFindElementsAutoHealing() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getFindElementsAutoHealing() != null
                ? settings.getFindElementsAutoHealing() : dynamicSettings.isFindElementsAutoHealing();
    }

    @Override
    public double getScoreCap() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getScoreCap() != null && settings.getScoreCap() > 0
                ? settings.getScoreCap() : dynamicSettings.getScoreCap();
    }

    @Override
    public int getRecoveryTries() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getRecoveryTries() != null && settings.getRecoveryTries() > 0
                ? settings.getRecoveryTries() : dynamicSettings.getRecoveryTries();
    }

    @Override
    public String getHealedType() {
        TenantSettingsDto settings = getSettings();
        return settings != null && settings.getHealedType() != null
                ? settings.getHealedType() : normalizeHealedType(dynamicSettings.getSelectorType());
    }

    /**
     * The default tenant changes only {@link DynamicSettings}; any other tenant changes only its database row.
     */
    @Override
    public TenantSettingsDto updateSettings(TenantSettingsDto request) {
        validateRequest(request);
        UUID tenantId = resolveTenantId();
        if (isDefaultTenant(tenantId)) {
            return updateDefaults(tenantId, request);
        }
        TenantSettingsDto updated = tenantTx.required(() -> {
            Settings settings = settingsRepository.findByTenantId(tenantId)
                    .orElseGet(() -> new Settings().setTenantId(tenantId));

            if (request.getHealEnabled() != null) {
                settings.setHealEnabled(request.getHealEnabled());
            }
            if (request.getRecoveryTries() != null) {
                settings.setRecoveryTries(request.getRecoveryTries());
            }
            if (request.getScoreCap() != null) {
                settings.setScoreCap(request.getScoreCap());
            }
            if (request.getHealedType() != null) {
                settings.setHealedType(normalizeHealedType(request.getHealedType()));
            }
            if (request.getKeySelectorUrl() != null) {
                settings.setKeySelectorUrl(request.getKeySelectorUrl());
            }
            if (request.getCollectMetrics() != null) {
                settings.setCollectMetrics(request.getCollectMetrics());
            }
            if (request.getFindElementsAutoHealing() != null) {
                settings.setFindElementsAutoHealing(request.getFindElementsAutoHealing());
            }

            Settings saved = settingsRepository.save(settings);
            log.info("[Settings] Updated settings for tenant {}: healEnabled={}, recoveryTries={}, scoreCap={}, healedType={}, keySelectorUrl={}, collectMetrics={}, findElementsAutoHealing={}",
                    tenantId, saved.getHealEnabled(), saved.getRecoveryTries(), saved.getScoreCap(), saved.getHealedType(),
                    saved.getKeySelectorUrl(), saved.getCollectMetrics(), saved.getFindElementsAutoHealing());
            return toDto(saved);
        });
        settingsCache.put(tenantId, updated);
        return updated;
    }

    @Override
    public Map<String, Object> updateSetting(String key, String value) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("success", false);

        try {
            switch (key) {
                case LOG_LEVEL -> result.put("success", setLogLevel(value));
                case KEY_SELECTOR_URL -> updateSettings(new TenantSettingsDto().setKeySelectorUrl(Boolean.parseBoolean(value)));
                case COLLECT_METRICS -> updateSettings(new TenantSettingsDto().setCollectMetrics(Boolean.parseBoolean(value)));
                case FIND_ELEMENTS_AUTO_HEALING -> updateSettings(new TenantSettingsDto().setFindElementsAutoHealing(Boolean.parseBoolean(value)));
                case SCORE_CAP -> updateSettings(new TenantSettingsDto().setScoreCap(Double.parseDouble(value)));
                case SELECTOR_TYPE, HEALED_TYPE -> updateSettings(new TenantSettingsDto().setHealedType(value));
                case RECOVERY_TRIES -> updateSettings(new TenantSettingsDto().setRecoveryTries(Integer.parseInt(value)));
                default -> {
                    log.warn("Unknown setting key: {}", key);
                    result.put("message", "Unknown setting key");
                    return result;
                }
            }
            if (!LOG_LEVEL.equals(key)) {
                result.put("success", true);
            }
            log.info("Configuration updated: {} = {}", key, value);
        } catch (Exception e) {
            log.error("Error updating setting: {}", key, e);
            result.put("message", e.getMessage());
        }
        return result;
    }

    private boolean setLogLevel(String logLevel) {
        if (logLevel == null || !VALID_LOG_LEVELS.contains(logLevel.toUpperCase())) {
            log.error("Invalid log level: {}", logLevel);
            return false;
        }
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLogger(HEALENIUM_LOGGER).setLevel(Level.toLevel(logLevel));
            System.setProperty("HLM_LOG_LEVEL", logLevel);
            return true;
        } catch (Exception e) {
            log.error("Error setting log level", e);
            return false;
        }
    }

    private TenantSettingsDto updateDefaults(UUID tenantId, TenantSettingsDto request) {
        if (request.getRecoveryTries() != null) {
            dynamicSettings.setRecoveryTries(request.getRecoveryTries());
        }
        if (request.getScoreCap() != null) {
            dynamicSettings.setScoreCap(request.getScoreCap());
        }
        if (request.getHealedType() != null) {
            dynamicSettings.setSelectorType("xpath".equals(normalizeHealedType(request.getHealedType())) ? "xpath" : "css");
        }
        if (request.getKeySelectorUrl() != null) {
            dynamicSettings.setKeySelectorUrl(request.getKeySelectorUrl());
        }
        if (request.getCollectMetrics() != null) {
            dynamicSettings.setCollectMetrics(request.getCollectMetrics());
        }
        if (request.getFindElementsAutoHealing() != null) {
            dynamicSettings.setFindElementsAutoHealing(request.getFindElementsAutoHealing());
        }
        TenantSettingsDto updated = buildDefaultDto(tenantId);
        log.info("[Settings] Updated default settings: recoveryTries={}, scoreCap={}, healedType={}, keySelectorUrl={}, collectMetrics={}, findElementsAutoHealing={}",
                updated.getRecoveryTries(), updated.getScoreCap(), updated.getHealedType(),
                updated.getKeySelectorUrl(), updated.getCollectMetrics(), updated.getFindElementsAutoHealing());
        return updated;
    }

    private boolean isDefaultTenant(UUID tenantId) {
        return TenantFilter.DEFAULT_TENANT_ID.equals(tenantId);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : TenantFilter.DEFAULT_TENANT_ID;
    }

    private void validateRequest(TenantSettingsDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Settings request body cannot be null");
        }
        if (request.getRecoveryTries() != null && request.getRecoveryTries() < 1) {
            throw new IllegalArgumentException("Recovery tries must be greater than or equal to 1");
        }
        if (request.getScoreCap() != null && (request.getScoreCap() < 0.0 || request.getScoreCap() > 1.0)) {
            throw new IllegalArgumentException("Score cap must be between 0.0 and 1.0");
        }
        if (request.getHealedType() != null) {
            String type = request.getHealedType().trim().toLowerCase();
            if (!"css".equals(type) && !"cssselector".equals(type) && !"xpath".equals(type)) {
                throw new IllegalArgumentException("Invalid healedType. Must be 'css' or 'xpath'");
            }
        }
    }

    private String normalizeHealedType(String type) {
        if (type == null) {
            return "css";
        }
        String trimmed = type.trim().toLowerCase();
        if ("cssselector".equals(trimmed)) {
            return "css";
        }
        return trimmed;
    }

    private TenantSettingsDto toDto(Settings entity) {
        return new TenantSettingsDto()
                .setTenantId(entity.getTenantId())
                .setHealEnabled(entity.getHealEnabled())
                .setRecoveryTries(entity.getRecoveryTries())
                .setScoreCap(entity.getScoreCap())
                .setHealedType(entity.getHealedType())
                .setKeySelectorUrl(entity.getKeySelectorUrl())
                .setCollectMetrics(entity.getCollectMetrics())
                .setFindElementsAutoHealing(entity.getFindElementsAutoHealing())
                .setUpdatedAt(entity.getUpdatedAt());
    }

    private TenantSettingsDto buildDefaultDto(UUID tenantId) {
        return new TenantSettingsDto()
                .setTenantId(tenantId)
                .setHealEnabled(true)
                .setRecoveryTries(dynamicSettings.getRecoveryTries())
                .setScoreCap(dynamicSettings.getScoreCap())
                .setHealedType(normalizeHealedType(dynamicSettings.getSelectorType()))
                .setKeySelectorUrl(dynamicSettings.isKeySelectorUrl())
                .setCollectMetrics(dynamicSettings.isCollectMetrics())
                .setFindElementsAutoHealing(dynamicSettings.isFindElementsAutoHealing());
    }
}
