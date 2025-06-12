package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.DedicatedInfo;
import com.epam.healenium.model.dto.elitea.EliteaDto;
import com.epam.healenium.model.dto.elitea.EliteaSelectorDetectionRequestDto;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LocatorPathsDto;

import java.util.List;

public interface EliteaService {
    //TODO Remove v1
    EliteaDto getEliteaDto(String authorizationHeader, String repoName);

    //TODO Remove v1
    EliteaDto getEliteaDto(String authorizationHeader, String gitRepositoryName, String reportId);

    List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(String authorizationHeader, String repoName, String reportId);

    EliteaDto getEliteaDto3(String authorizationHeader, String repoName, String reportId);

    List<HealingResult> getDedicatedInfo(String reportId);

    void saveLocatorPaths(List<LocatorPathsDto> request);

}
