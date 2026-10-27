package com.epam.healenium.service;

import com.epam.healenium.model.dto.LlmDto;
import com.epam.healenium.model.dto.VcsDto;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IntegrationService {

    @NotNull List<VcsDto> createVcs(VcsDto vcsDto);

    @NotNull List<VcsDto> updateVcs(String id, VcsDto vcsDto);

    @NotNull List<LlmDto> createLlm(LlmDto llmDto);

    @NotNull List<LlmDto> updateLlm(String id, LlmDto llmDto);

    VcsDto getActiveVcs();

    @NotNull List<VcsDto> getVcsAll();

    @NotNull List<VcsDto> activateVcs(String id);

    void deleteVcs(String id);

    boolean hasActiveGit();

    boolean hasActiveGenAI();

    LlmDto getActiveLlm();

    List<LlmDto> getLlmAll();

    List<LlmDto> setActiveLlm(String id);

    void deleteLlm(String id);
}
