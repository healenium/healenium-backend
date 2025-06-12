package com.epam.healenium.controller;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.DedicatedInfo;
import com.epam.healenium.model.dto.elitea.EliteaDto;
import com.epam.healenium.model.dto.elitea.EliteaSelectorDetectionRequestDto;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LocatorPathsDto;
import com.epam.healenium.service.CredentialService;
import com.epam.healenium.service.EliteaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final CredentialService credentialService;

    //TODO Remove v1
    @GetMapping("/{projectName}/{repoName}")
    public EliteaDto getLastEliteaReport(@RequestHeader("Authorization") String authorizationHeader,
                                         @PathVariable String projectName,
                                         @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        log.info("[ELITEA] GitRepoName {1}" + authorizationHeader, gitRepositoryName);
        return eliteaService.getEliteaDto(authorizationHeader, gitRepositoryName);
    }

    //TODO Remove v1
    @GetMapping("/{reportId}/{projectName}/{repoName}")
    public EliteaDto geEliteaReportById(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable String reportId,
                                        @PathVariable String projectName,
                                        @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        log.info("[ELITEA] ReportId: {0}, GitRepoName {1}" + reportId, gitRepositoryName);
        return eliteaService.getEliteaDto(authorizationHeader, gitRepositoryName, reportId);
    }

    @GetMapping("/v2/{reportId}/{projectName}/{repoName}")
    public List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(@RequestHeader("Authorization") String authorizationHeader,
                                                                             @PathVariable String reportId,
                                                                             @PathVariable String projectName,
                                                                             @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        return eliteaService.selectorDetectionByReport(authorizationHeader, gitRepositoryName, reportId);
    }

    @GetMapping("/v2/mr/{reportId}/{projectName}/{repoName}")
    public EliteaDto getFinalTableForMR(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable String reportId,
                                          @PathVariable String projectName,
                                          @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        return eliteaService.getEliteaDto3(authorizationHeader, gitRepositoryName, reportId);
    }

    @GetMapping("/dedicated-info/{reportId}")
    public DedicatedInfo getDedicatedInfo(@PathVariable String reportId) {
        List<HealingResult> invalidSelectorClass = eliteaService.getDedicatedInfo(reportId);
        log.info("[ELITEA] Invalid Selector Classes: " + invalidSelectorClass);
        return new DedicatedInfo().setAvailableForMr(invalidSelectorClass.isEmpty());
    }

    @PatchMapping("/paths/save/{reportId}")
    public DedicatedInfo saveLocatorPaths(@PathVariable String reportId, @Valid @RequestBody List<LocatorPathsDto> detectionPaths) {
        log.info("[ELITEA] Save Locator Paths: " + detectionPaths.stream().map(LocatorPathsDto::getSelectedPath).toList());
        eliteaService.saveLocatorPaths(detectionPaths);
        List<HealingResult> invalidSelectorClass = eliteaService.getDedicatedInfo(reportId);
        return new DedicatedInfo().setAvailableForMr(invalidSelectorClass.isEmpty());
    }

    @PostMapping("/credential/save")
    public void saveCredential(@Valid @RequestBody IntegrationFormDto integrationFormDto) {
        log.info("[ELITEA] Save Credential: " + integrationFormDto);
        credentialService.saveOrUpdateGitHub(integrationFormDto);
        credentialService.saveOrUpdateElitea(integrationFormDto);
    }

    @GetMapping("/credentials")
    public IntegrationFormDto getCredentials() {
        IntegrationFormDto integrationFormDto = credentialService.getCredentials();
        log.info("[ELITEA] Save Credential: " + integrationFormDto);
        return integrationFormDto;
    }

}
