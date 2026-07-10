package com.epam.healenium.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.service.SettingService;
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

    private static final List<String> VALID_LOG_LEVELS = Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
    private static final String HEALENIUM_LOGGER = "com.epam.healenium";
    
    private static final String KEY_SELECTOR_URL = "KEY_SELECTOR_URL";
    private static final String COLLECT_METRICS = "COLLECT_METRICS";
    private static final String FIND_ELEMENTS_AUTO_HEALING = "FIND_ELEMENTS_AUTO_HEALING";
    private static final String SCORE_CAP = "SCORE_CAP";
    private static final String SELECTOR_TYPE = "SELECTOR_TYPE";
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
                    boolean keySelectorUrlSuccess = setKeySelectorUrl(Boolean.parseBoolean(value));
                    result.put("success", keySelectorUrlSuccess);
                    break;
                case COLLECT_METRICS:
                    boolean collectMetricsSuccess = setCollectMetrics(Boolean.parseBoolean(value));
                    result.put("success", collectMetricsSuccess);
                    break;
                case FIND_ELEMENTS_AUTO_HEALING:
                    boolean findElementsAutoHealingSuccess = setFindElementsAutoHealing(Boolean.parseBoolean(value));
                    result.put("success", findElementsAutoHealingSuccess);
                    break;
                case SCORE_CAP:
                    boolean scoreCapSuccess = setScoreCap(Double.parseDouble(value));
                    result.put("success", scoreCapSuccess);
                    break;
                case SELECTOR_TYPE:
                    boolean selectorTypeSuccess = setSelectorType(value);
                    result.put("success", selectorTypeSuccess);
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
        Map<String, Object> settings = new HashMap<>();
        settings.put(LOG_LEVEL, System.getProperty("HLM_LOG_LEVEL", "INFO"));
        settings.put(KEY_SELECTOR_URL, dynamicSettings.isKeySelectorUrl());
        settings.put(COLLECT_METRICS, dynamicSettings.isCollectMetrics());
        settings.put(FIND_ELEMENTS_AUTO_HEALING, dynamicSettings.isFindElementsAutoHealing());
        settings.put(SCORE_CAP, dynamicSettings.getScoreCap());
        settings.put(SELECTOR_TYPE, dynamicSettings.getSelectorType());
        return settings;
    }

    private boolean isValidSelectorType(String selectorType) {
        return StringUtils.hasText(selectorType)
                && ("cssSelector".equals(selectorType) || "xpath".equals(selectorType));
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
