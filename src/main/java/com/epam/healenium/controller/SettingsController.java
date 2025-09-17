package com.epam.healenium.controller;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.*;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/healenium/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final ReportService reportService;
    private final SelectorService selectorService;
    private final SettingsService settingsService;

    @GetMapping("/dedicated-info/{reportId}")
    public ResponseEntity<DedicatedInfo> getDedicatedInfo(@PathVariable String reportId) {
        log.info("[SETTINGS] Getting dedicated info for report: {}", reportId);
        
        if (reportId == null || reportId.trim().isEmpty()) {
            throw new IllegalArgumentException("Report ID cannot be null or empty");
        }
        
        boolean availableForSd = settingsService.isAvailableForSd();
        boolean availableForMr = availableForSd && reportService.getDedicatedInfo(reportId).isEmpty();
        
        DedicatedInfo dedicatedInfo = new DedicatedInfo()
                .setAvailableForSd(availableForSd)
                .setAvailableForMr(availableForMr);
                
        return ResponseEntity.ok(dedicatedInfo);
    }

    @PatchMapping("/paths/save/{reportId}")
    public ResponseEntity<DedicatedInfo> saveLocatorPaths(@PathVariable String reportId, 
                                                         @Valid @RequestBody List<LocatorPathsDto> detectionPaths) {
        log.info("[SETTINGS] Saving locator paths for report: {}, paths: {}", 
                reportId, detectionPaths.stream().map(LocatorPathsDto::getSelectedPath).toList());
        
        if (reportId == null || reportId.trim().isEmpty()) {
            throw new IllegalArgumentException("Report ID cannot be null or empty");
        }
        
        if (detectionPaths == null || detectionPaths.isEmpty()) {
            throw new IllegalArgumentException("Detection paths cannot be null or empty");
        }

        selectorService.saveLocatorPaths(detectionPaths, reportId);
        List<HealingResult> invalidSelectorClass = reportService.getDedicatedInfo(reportId);
        
        DedicatedInfo dedicatedInfo = new DedicatedInfo()
                .setAvailableForMr(invalidSelectorClass.isEmpty());
                
        return ResponseEntity.ok(dedicatedInfo);
    }

    @PostMapping("/vcs/save")
    public ResponseEntity<Void> saveVcsCredentials(@Valid @RequestBody VcsDto vcsDto) {
        log.info("[SETTINGS] Saving VCS credentials for repository: {}", vcsDto.getRepository());
        settingsService.saveOrUpdateVcs(vcsDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/llm/save")
    public ResponseEntity<List<LlmDto>> saveLlmCredentials(@Valid @RequestBody LlmDto llmDto) {
        log.info("[SETTINGS] Saving LLM credentials for model: {}", llmDto.getName());
        List<LlmDto> result = settingsService.saveOrUpdateLlm(llmDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vcs/{platform}")
    public ResponseEntity<VcsDto> getVcs(@PathVariable String platform) {
        log.info("[SETTINGS] Getting VCS configuration for platform: {}", platform);
        VcsDto vcsDto = settingsService.getVcs(platform);
        return ResponseEntity.ok(vcsDto);
    }

    @GetMapping("/llm/{platform}")
    public ResponseEntity<LlmDto> getLlm(@PathVariable String platform) {
        log.info("[SETTINGS] Getting LLM configuration for platform: {}", platform);
        LlmDto llmDto = settingsService.getLlm(platform);
        return ResponseEntity.ok(llmDto);
    }

    @GetMapping("/llm/all")
    public ResponseEntity<List<LlmDto>> getAllLlms() {
        log.info("[SETTINGS] Getting all LLM configurations");
        List<LlmDto> llmDtos = settingsService.getLlmAll();
        return ResponseEntity.ok(llmDtos);
    }

    @PostMapping("/llm/activate/{id}")
    public ResponseEntity<List<LlmDto>> activateLlm(@PathVariable String id) {
        log.info("[SETTINGS] Activating LLM with id: {}", id);
        List<LlmDto> llmDtos = settingsService.setActiveLlm(id);
        return ResponseEntity.ok(llmDtos);
    }

    @GetMapping("/llm/active")
    public ResponseEntity<LlmDto> getActiveLlm() {
        log.info("[SETTINGS] Getting active LLM configuration");
        LlmDto activeLlm = settingsService.getActiveLlm();
        return ResponseEntity.ok(activeLlm);
    }

    @DeleteMapping("/llm/delete/{id}")
    public ResponseEntity<Void> deleteLlm(@PathVariable String id) {
        log.info("[SETTINGS] Deleting LLM with id: {}", id);
        settingsService.deleteLlm(id);
        return ResponseEntity.ok().build();
    }

}
