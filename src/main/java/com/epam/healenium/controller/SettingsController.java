package com.epam.healenium.controller;

import com.epam.healenium.model.dto.TenantSettingsDto;
import com.epam.healenium.service.SettingService;
import com.epam.healenium.service.TenantSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/healenium/settings", "/healenium/tenant/settings"})
@RequiredArgsConstructor
public class SettingsController {

    private final SettingService settingService;
    private final TenantSettingsService tenantSettingsService;

    /**
     * Get settings for the current tenant or default values if not configured yet.
     *
     * @return settings DTO
     */
    @GetMapping
    public ResponseEntity<TenantSettingsDto> getSettings() {
        log.debug("[Settings] GET request");
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
        log.debug("[Settings] PUT request: {}", request);
        TenantSettingsDto updated = tenantSettingsService.updateSettings(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update a configuration setting (legacy endpoint supporting JSON body or query params)
     *
     * @param request Map containing "key" and "value" for the configuration to update
     * @param paramKey optional query parameter "key"
     * @param paramValue optional query parameter "value"
     * @return ResponseEntity with status message
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSetting(
            @RequestBody(required = false) Map<String, String> request,
            @RequestParam(value = "key", required = false) String paramKey,
            @RequestParam(value = "value", required = false) String paramValue) {
        String key = (request != null && request.containsKey("key")) ? request.get("key") : paramKey;
        String value = (request != null && request.containsKey("value")) ? request.get("value") : paramValue;
        log.debug("[Settings] Update: key={}, value={}", key, value);
        Map<String, Object> result = settingService.updateSetting(key, value);

        return ResponseEntity.ok(result);
    }

}