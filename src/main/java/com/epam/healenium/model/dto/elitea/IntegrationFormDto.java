package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class IntegrationFormDto {
    private String gitHubRepository;
    private String gitHubAccessToken;
    private String gitHubBranch;
    private String eliteaToken;
}
