package com.epam.healenium.service;

import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.VcsDto;

public interface SettingsService {

    void saveOrUpdateVcs(VcsDto vcsDto);

    void saveOrUpdateLlm(LlmDto llmDto);

    IntegrationFormDto getSettings(String platform);
    VcsDto getVcs(String platform);
    LlmDto getLlm(String platform);
}
