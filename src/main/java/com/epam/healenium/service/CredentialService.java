package com.epam.healenium.service;

import com.epam.healenium.model.dto.elitea.IntegrationFormDto;

public interface CredentialService {

    void saveOrUpdateGitHub(IntegrationFormDto integrationFormDto);

    void saveOrUpdateElitea(IntegrationFormDto integrationFormDto);

    IntegrationFormDto getCredentials();
}
