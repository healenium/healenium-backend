package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.*;

import java.util.List;

public interface EliteaService {

    List<EliteaSelectorDetectionRequestDto> selectorDetectionByReport(String reportId);

    EliteaDto createPullRequest(String reportId);

    List<HealingResult> getDedicatedInfo(String reportId);

    void saveLocatorPaths(List<LocatorPathsDto> request);

    void updateGitHubToolkit(VcsDto vcsDto);

}
