package com.epam.healenium.model.dto;

import lombok.extern.slf4j.Slf4j;

/**
 * Per-run settings carried in the {@code selector-candidates} body ({@code healedType}, {@code scoreCap},
 * {@code recoveryTries}). They take priority over the tenant settings. Invalid values are ignored.
 */
@Slf4j
public record SettingsOverride(String healedType, Double scoreCap, Integer recoveryTries) {

    public static final SettingsOverride NONE = new SettingsOverride(null, null, null);

    public static SettingsOverride from(String healedType, Double scoreCap, Integer recoveryTries) {
        return new SettingsOverride(healedType(healedType), scoreCap(scoreCap), recoveryTries(recoveryTries));
    }

    private static String healedType(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String type = raw.trim().toLowerCase();
        if ("xpath".equals(type)) {
            return "xpath";
        }
        if ("css".equals(type) || "cssselector".equals(type)) {
            return "css";
        }
        log.warn("[Settings] Ignoring invalid healedType: {}", raw);
        return null;
    }

    private static Double scoreCap(Double raw) {
        if (raw == null) {
            return null;
        }
        if (raw >= 0.0 && raw <= 1.0) {
            return raw;
        }
        log.warn("[Settings] Ignoring invalid scoreCap: {}", raw);
        return null;
    }

    private static Integer recoveryTries(Integer raw) {
        if (raw == null) {
            return null;
        }
        if (raw >= 1) {
            return raw;
        }
        log.warn("[Settings] Ignoring invalid recoveryTries: {}", raw);
        return null;
    }
}
