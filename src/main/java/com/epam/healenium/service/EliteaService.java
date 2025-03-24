package com.epam.healenium.service;

import com.epam.healenium.model.dto.elitea.EliteaDto;

public interface EliteaService {
    EliteaDto getEliteaDto(String authorizationHeader, String repoName);

    EliteaDto getEliteaDto(String authorizationHeader, String gitRepositoryName, String reportId);

}
