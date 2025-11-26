package com.epam.healenium.controller;

import com.epam.healenium.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healenium/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingService settingService;

    /**
     * Update a configuration setting
     * @param request Map containing "key" and "value" for the configuration to update
     * @return ResponseEntity with status message
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSetting(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String value = request.get("value");
        Map<String, Object> result = settingService.updateSetting(key, value);

        return ResponseEntity.ok(result);
    }
    
    /**
     * Get all current configuration settings
     * @return ResponseEntity with all settings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings() {
        Map<String, Object> settings = settingService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

}