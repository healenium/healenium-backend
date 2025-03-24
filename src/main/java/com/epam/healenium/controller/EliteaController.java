package com.epam.healenium.controller;

import com.epam.healenium.model.dto.elitea.EliteaDto;
import com.epam.healenium.service.EliteaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/healenium/elitea")
@RequiredArgsConstructor
public class EliteaController {

    private final EliteaService eliteaService;

    @GetMapping("/{projectName}/{repoName}")
    public EliteaDto getLastEliteaReport(@RequestHeader("Authorization") String authorizationHeader,
                                         @PathVariable String projectName,
                                         @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        log.info("[ELITEA] GitRepoName {1}" + authorizationHeader, gitRepositoryName);
        return eliteaService.getEliteaDto(authorizationHeader, gitRepositoryName);
    }

    @GetMapping("/{reportId}/{projectName}/{repoName}")
    public EliteaDto geEliteaReportById(@RequestHeader("Authorization") String authorizationHeader,
                                        @PathVariable String reportId,
                                        @PathVariable String projectName,
                                        @PathVariable String repoName) {
        String gitRepositoryName = projectName + "/" + repoName;
        log.info("[ELITEA] ReportId: {0}, GitRepoName {1}" + reportId, gitRepositoryName);
        return eliteaService.getEliteaDto(authorizationHeader, gitRepositoryName, reportId);
    }
}
