package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.Credential;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.repository.CredentialRepository;
import com.epam.healenium.service.CredentialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class CredentialServiceImpl implements CredentialService {

    private static final String GITHUB_NAME = "GitHub";
    private static final String ELITEA_NAME = "Elitea";

    private final CredentialRepository credentialRepository;

    @Override
    public void saveOrUpdateGitHub(IntegrationFormDto integrationFormDto) {
        if (integrationFormDto.getGitHubRepository() != null || integrationFormDto.getGitHubAccessToken() != null) {
            Optional<Credential> existingGitHubCredential = credentialRepository.findByName(GITHUB_NAME);

            if (existingGitHubCredential.isPresent()) {
                Credential credential = existingGitHubCredential.get();
                credential.setToken(integrationFormDto.getGitHubAccessToken());
                credential.setResource(integrationFormDto.getGitHubRepository());
                credentialRepository.save(credential);
            } else {
                Credential newCredential = new Credential()
                        .setName(GITHUB_NAME)
                        .setToken(integrationFormDto.getGitHubAccessToken())
                        .setResource(integrationFormDto.getGitHubRepository())
                        .setUid(generateUid());
                credentialRepository.save(newCredential);
            }
        }
    }

    @Override
    public void saveOrUpdateElitea(IntegrationFormDto integrationFormDto) {
        if (integrationFormDto.getEliteaToken() != null) {
            Optional<Credential> existingEliteaCredential = credentialRepository.findByName(ELITEA_NAME);
            if (existingEliteaCredential.isPresent()) {
                Credential credential = existingEliteaCredential.get();
                credential.setToken(integrationFormDto.getEliteaToken());
                credentialRepository.save(credential);
            } else {
                Credential newCredential = new Credential()
                        .setName(ELITEA_NAME)
                        .setToken(integrationFormDto.getEliteaToken())
                        .setUid(generateUid());
                credentialRepository.save(newCredential);
            }
        }
    }

    private String generateUid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public IntegrationFormDto getCredentials() {
        Optional<Credential> gitHub = credentialRepository.findByName(GITHUB_NAME);
        Optional<Credential> elitea = credentialRepository.findByName(ELITEA_NAME);

        IntegrationFormDto integrationFormDto = new IntegrationFormDto();
        gitHub.ifPresent(g -> {
            integrationFormDto.setGitHubAccessToken(g.getToken());
            integrationFormDto.setGitHubRepository(g.getResource());
        });
        elitea.ifPresent(e -> {
            integrationFormDto.setEliteaToken(e.getToken());
        });
        return integrationFormDto;
    }
}
