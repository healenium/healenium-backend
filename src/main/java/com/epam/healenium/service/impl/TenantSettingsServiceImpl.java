package com.epam.healenium.service.impl;

import com.epam.healenium.config.DynamicSettings;
import com.epam.healenium.model.domain.Settings;
import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.repository.SettingsRepository;
import com.epam.healenium.service.TenantSettingsService;
import com.epam.healenium.tenant.TenantContext;
import com.epam.healenium.tenant.TenantFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSettingsServiceImpl implements TenantSettingsService {

    private final SettingsRepository settingsRepository;
    private final DynamicSettings dynamicSettings;

    @Override
    @Transactional(readOnly = true)
    public TenantSettingsDto getSettings() {
        UUID tenantId = resolveTenantId();
        return settingsRepository.findByTenantId(tenantId)
                .map(this::toDto)
                .orElseGet(() -> buildDefaultDto(tenantId));
    }

    @Override
    @Transactional
    public TenantSettingsDto updateSettings(TenantSettingsDto request) {
        validateRequest(request);
        UUID tenantId = resolveTenantId();

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
                .setRecoveryTries(dynamicSettings.getRecoveryTries() > 0 ? dynamicSettings.getRecoveryTries() : 1)
                .setScoreCap(dynamicSettings.getScoreCap() > 0 ? dynamicSettings.getScoreCap() : 0.60)
                .setHealedType(normalizeHealedType(Objects.requireNonNullElse(dynamicSettings.getSelectorType(), "css")))
                .setKeySelectorUrl(dynamicSettings.isKeySelectorUrl())
                .setCollectMetrics(dynamicSettings.isCollectMetrics())
                .setFindElementsAutoHealing(dynamicSettings.isFindElementsAutoHealing());
    }
}
