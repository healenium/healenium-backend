package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.VcsDto;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SettingsService {

    void saveOrUpdateVcs(VcsDto vcsDto);

    @NotNull List<LlmDto> saveOrUpdateLlm(LlmDto llmDto);

    IntegrationFormDto getSettings(String platform);
    VcsDto getVcs(String platform);

    boolean isAvailableForSd();

    LlmDto getLlm(String platform);

    List<LlmDto> getLlmAll();

    List<LlmDto> setActiveLlm(String id);
    LlmDto getActiveLlm();

    void deleteLlm(String id);
}
