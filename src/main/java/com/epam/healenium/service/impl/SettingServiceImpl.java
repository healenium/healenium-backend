package com.epam.healenium.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.service.SettingService;
import com.epam.healenium.service.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final DynamicSettings dynamicSettings;
    private final TenantSettingsService tenantSettingsService;

    private static final List<String> VALID_LOG_LEVELS = Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
    private static final String HEALENIUM_LOGGER = "com.epam.healenium";
    
    private static final String KEY_SELECTOR_URL = "KEY_SELECTOR_URL";
    private static final String COLLECT_METRICS = "COLLECT_METRICS";
    private static final String FIND_ELEMENTS_AUTO_HEALING = "FIND_ELEMENTS_AUTO_HEALING";
    private static final String SCORE_CAP = "SCORE_CAP";
    private static final String SELECTOR_TYPE = "SELECTOR_TYPE";
    private static final String HEALED_TYPE = "HEALED_TYPE";
    private static final String RECOVERY_TRIES = "RECOVERY_TRIES";
    private static final String LOG_LEVEL = "LOG_LEVEL";

    /**
     * Set log level for healenium loggers
     *
     * @param logLevel The log level to set
     * @return true if successful, false otherwise
     */
    @Override
    public boolean setLogLevel(String logLevel) {
        if (!isValidLogLevel(logLevel)) {
            log.error("Invalid log level: {}", logLevel);
            return false;
        }

        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Level level = Level.toLevel(logLevel);

            ch.qos.logback.classic.Logger healeniumLogger = loggerContext.getLogger(HEALENIUM_LOGGER);
            healeniumLogger.setLevel(level);
            
            System.setProperty("HLM_LOG_LEVEL", logLevel);
            return true;
        } catch (Exception e) {
            log.error("Error setting log level", e);
            return false;
        }
    }
    
    @Override
    public boolean setKeySelectorUrl(boolean enabled) {
        try {
            dynamicSettings.setKeySelectorUrl(enabled);
            return true;
        } catch (Exception e) {
            log.error("Error setting KEY_SELECTOR_URL", e);
            return false;
        }
    }
    
    @Override
    public boolean setCollectMetrics(boolean enabled) {
        try {
            dynamicSettings.setCollectMetrics(enabled);
            return true;
        } catch (Exception e) {
            log.error("Error setting COLLECT_METRICS", e);
            return false;
        }
    }
    
    @Override
    public boolean setFindElementsAutoHealing(boolean enabled) {
        try {
            dynamicSettings.setFindElementsAutoHealing(enabled);
            return true;
        } catch (Exception e) {
            log.error("Error setting FIND_ELEMENTS_AUTO_HEALING", e);
            return false;
        }
    }

    @Override
    public boolean setScoreCap(double scoreCap) {
        if (scoreCap < 0.0 || scoreCap > 1.0) {
            log.error("Invalid score cap: {}. Value must be between 0.0 and 1.0", scoreCap);
            return false;
        }

        try {
            dynamicSettings.setScoreCap(scoreCap);
            return true;
        } catch (Exception e) {
            log.error("Error setting SCORE_CAP", e);
            return false;
        }
    }

    @Override
    public boolean setSelectorType(String selectorType) {
        if (!isValidSelectorType(selectorType)) {
            log.error("Invalid selector type: {}. Value must be 'cssSelector' or 'xpath'", selectorType);
            return false;
        }

        try {
            dynamicSettings.setSelectorType(selectorType);
            return true;
        } catch (Exception e) {
            log.error("Error setting SELECTOR_TYPE", e);
            return false;
        }
    }

    @Override
    public boolean setRecoveryTries(int recoveryTries) {
        if (recoveryTries < 0) {
            log.error("Invalid recovery tries: {}. Value must be a non-negative integer", recoveryTries);
            return false;
        }

        try {
            dynamicSettings.setRecoveryTries(recoveryTries);
            return true;
        } catch (Exception e) {
            log.error("Error setting RECOVERY_TRIES", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> updateSetting(String key, String value) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("success", false);
        
        try {
            switch (key) {
                case LOG_LEVEL:
                    boolean logLevelSuccess = setLogLevel(value);
                    result.put("success", logLevelSuccess);
                    break;
                case KEY_SELECTOR_URL:
                    boolean keySelectorUrl = Boolean.parseBoolean(value);
                    setKeySelectorUrl(keySelectorUrl);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setKeySelectorUrl(keySelectorUrl));
                    result.put("success", true);
                    break;
                case COLLECT_METRICS:
                    boolean collectMetrics = Boolean.parseBoolean(value);
                    setCollectMetrics(collectMetrics);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setCollectMetrics(collectMetrics));
                    result.put("success", true);
                    break;
                case FIND_ELEMENTS_AUTO_HEALING:
                    boolean autoHealing = Boolean.parseBoolean(value);
                    setFindElementsAutoHealing(autoHealing);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setFindElementsAutoHealing(autoHealing));
                    result.put("success", true);
                    break;
                case SCORE_CAP:
                    double scoreCap = Double.parseDouble(value);
                    setScoreCap(scoreCap);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setScoreCap(scoreCap));
                    result.put("success", true);
                    break;
                case SELECTOR_TYPE:
                case HEALED_TYPE:
                    setSelectorType(value);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setHealedType(value));
                    result.put("success", true);
                    break;
                case RECOVERY_TRIES:
                    int tries = Integer.parseInt(value);
                    setRecoveryTries(tries);
                    tenantSettingsService.updateSettings(new TenantSettingsDto().setRecoveryTries(tries));
                    result.put("success", true);
                    break;
                default:
                    log.warn("Unknown setting key: {}", key);
                    result.put("message", "Unknown setting key");
            }

            if (result.get("success") != null) {
                log.warn("Configuration updated: {} = {}", key, value);
            }
        } catch (Exception e) {
            log.error("Error updating setting: " + key, e);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getAllSettings() {
        TenantSettingsDto ts = tenantSettingsService.getSettings();
        Map<String, Object> settings = new HashMap<>();
        settings.put(LOG_LEVEL, System.getProperty("HLM_LOG_LEVEL", "INFO"));
        settings.put(KEY_SELECTOR_URL, ts.getKeySelectorUrl() != null ? ts.getKeySelectorUrl() : dynamicSettings.isKeySelectorUrl());
        settings.put(COLLECT_METRICS, ts.getCollectMetrics() != null ? ts.getCollectMetrics() : dynamicSettings.isCollectMetrics());
        settings.put(FIND_ELEMENTS_AUTO_HEALING, ts.getFindElementsAutoHealing() != null ? ts.getFindElementsAutoHealing() : dynamicSettings.isFindElementsAutoHealing());
        settings.put(SCORE_CAP, ts.getScoreCap() != null ? ts.getScoreCap() : dynamicSettings.getScoreCap());
        settings.put(SELECTOR_TYPE, ts.getHealedType() != null ? ts.getHealedType() : dynamicSettings.getSelectorType());
        settings.put(RECOVERY_TRIES, ts.getRecoveryTries() != null ? ts.getRecoveryTries() : dynamicSettings.getRecoveryTries());
        return settings;
    }

    private boolean isValidSelectorType(String selectorType) {
        return StringUtils.hasText(selectorType)
                && ("css".equalsIgnoreCase(selectorType) || "cssSelector".equalsIgnoreCase(selectorType) || "xpath".equalsIgnoreCase(selectorType));
    }

    /**
     * Check if the provided log level is valid
     *
     * @param logLevel Log level to check
     * @return true if valid, false otherwise
     */
    public boolean isValidLogLevel(String logLevel) {
        return logLevel != null && VALID_LOG_LEVELS.contains(logLevel.toUpperCase());
    }

}
