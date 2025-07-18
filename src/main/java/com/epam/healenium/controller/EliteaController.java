package com.epam.healenium.controller;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.*;
import com.epam.healenium.service.SettingsService;
import com.epam.healenium.service.EliteaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/healenium/elitea")
@RequiredArgsConstructor
public class EliteaController {

    private final EliteaService eliteaService;
    private final SettingsService settingsService;

    @GetMapping("/selector-detection/{reportId}")
    public List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(@PathVariable String reportId) {
        return eliteaService.selectorDetectionByReport(reportId);
    }

    @GetMapping("/pull-request/{reportId}")
    public EliteaDto createPullRequest(@PathVariable String reportId) {
        return eliteaService.createPullRequest(reportId);
    }

    @GetMapping("/dedicated-info/{reportId}")
    public DedicatedInfo getDedicatedInfo(@PathVariable String reportId) {
        List<HealingResult> invalidSelectorClass = eliteaService.getDedicatedInfo(reportId);
        return new DedicatedInfo().setAvailableForMr(invalidSelectorClass.isEmpty());
    }

    @PatchMapping("/paths/save/{reportId}")
    public DedicatedInfo saveLocatorPaths(@PathVariable String reportId, @Valid @RequestBody List<LocatorPathsDto> detectionPaths) {
        log.info("[ELITEA] Save Locator Paths: " + detectionPaths.stream().map(LocatorPathsDto::getSelectedPath).toList());
        eliteaService.saveLocatorPaths(detectionPaths);
        List<HealingResult> invalidSelectorClass = eliteaService.getDedicatedInfo(reportId);
        return new DedicatedInfo().setAvailableForMr(invalidSelectorClass.isEmpty());
    }

    @PostMapping("/vcs/save")
    public void saveCredential(@Valid @RequestBody VcsDto vcsDto) {
        settingsService.saveOrUpdateVcs(vcsDto);
        eliteaService.updateGitHubToolkit(vcsDto);
    }

    @PostMapping("/llm/save")
    public void saveCredential(@Valid @RequestBody LlmDto llmDto) {
        settingsService.saveOrUpdateLlm(llmDto);
    }

    @GetMapping("/vcs/{platform}")
    public VcsDto getVcs(@PathVariable String platform) {
        VcsDto vcsDto = settingsService.getVcs(platform);
        return vcsDto;
    }

    @GetMapping("/llm/{platform}")
    public LlmDto getLlm(@PathVariable String platform) {
        LlmDto llmDto = settingsService.getLlm(platform);
        return llmDto;
    }

}
