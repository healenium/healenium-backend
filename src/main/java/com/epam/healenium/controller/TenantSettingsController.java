package com.epam.healenium.controller;

import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.service.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/healenium/tenant/settings")
@RequiredArgsConstructor
public class TenantSettingsController {

    private final TenantSettingsService tenantSettingsService;

    /**
     * Get settings for the current tenant or default values if not configured yet.
     *
     * @return settings DTO
     */
    @GetMapping
    public ResponseEntity<TenantSettingsDto> getSettings() {
        log.debug("[TenantSettings] GET request");
        TenantSettingsDto settings = tenantSettingsService.getSettings();
        return ResponseEntity.ok(settings);
    }

    /**
     * Update or create settings for the current tenant.
     *
     * @param request settings update request
     * @return updated settings DTO
     */
    @PutMapping
    public ResponseEntity<TenantSettingsDto> updateSettings(@RequestBody TenantSettingsDto request) {
        log.debug("[TenantSettings] PUT request: {}", request);
        TenantSettingsDto updated = tenantSettingsService.updateSettings(request);
        return ResponseEntity.ok(updated);
    }
}
