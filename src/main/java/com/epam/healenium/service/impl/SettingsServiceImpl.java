package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Llm;
import com.epam.healenium.model.domain.Report;
import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.VcsDto;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.epam.healenium.repository.LlmRepository;
import com.epam.healenium.repository.VcsRepository;
import com.epam.healenium.service.SettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
@Transactional
public class SettingsServiceImpl implements SettingsService {

    private final VcsRepository vcsRepository;
    private final LlmRepository llmRepository;

    @Override
    public void saveOrUpdateVcs(VcsDto vcsDto) {
        if (!isVcsDtoValid(vcsDto)) {
            log.warn("[SETTINGS] Invalid VCS DTO provided, skipping save operation");
            return;
        }

        Optional<Vcs> existingVcs = vcsRepository.findAll().stream().findFirst();
        
        if (existingVcs.isPresent()) {
            updateExistingVcs(existingVcs.get(), vcsDto);
        } else {
            createNewVcs(vcsDto);
        }
    }

    private boolean isVcsDtoValid(VcsDto vcsDto) {
        return vcsDto != null && (
                vcsDto.getRepository() != null || 
                vcsDto.getAccessToken() != null || 
                vcsDto.getBranch() != null
        );
    }

    private void updateExistingVcs(Vcs existingVcs, VcsDto vcsDto) {
        log.info("[SETTINGS] Updating existing VCS configuration");
        
        if (vcsDto.getAccessToken() != null) {
            existingVcs.setAccessToken(vcsDto.getAccessToken());
        }
        if (vcsDto.getRepository() != null) {
            existingVcs.setRepository(vcsDto.getRepository());
        }
        if (vcsDto.getBranch() != null) {
            existingVcs.setBranch(vcsDto.getBranch());
        }
        
        vcsRepository.save(existingVcs);
    }

    private void createNewVcs(VcsDto vcsDto) {
        log.info("[SETTINGS] Creating new VCS configuration");
        
        Vcs newVcs = new Vcs()
                .setUid(generateUid())
                .setName("github")
                .setRepository(vcsDto.getRepository())
                .setBranch(vcsDto.getBranch())
                .setAccessToken(vcsDto.getAccessToken());
                
        vcsRepository.save(newVcs);
    }

    @Override
    public List<LlmDto> saveOrUpdateLlm(LlmDto llmDto) {
        if (!isLlmDtoValid(llmDto)) {
            log.warn("[SETTINGS] Invalid LLM DTO provided, skipping save operation");
            return getAllLlms();
        }

        Optional<Llm> existingLlm = findLlmByName(llmDto.getName());
        deactivateAllModels();
        
        if (existingLlm.isPresent()) {
            updateExistingLlm(existingLlm.get(), llmDto);
        } else {
            createNewLlm(llmDto);
        }
        
        return getAllLlms();
    }

    private boolean isLlmDtoValid(LlmDto llmDto) {
        return llmDto != null && 
               llmDto.getAccessToken() != null && 
               !llmDto.getAccessToken().trim().isEmpty() &&
               llmDto.getName() != null && 
               !llmDto.getName().trim().isEmpty();
    }

    private Optional<Llm> findLlmByName(String name) {
        return llmRepository.findAll().stream()
                .filter(llm -> llm.getName().equals(name))
                .findFirst();
    }

    private void updateExistingLlm(Llm existingLlm, LlmDto llmDto) {
        log.info("[SETTINGS] Updating existing LLM: {}", existingLlm.getName());
        
        existingLlm.setAccessToken(llmDto.getAccessToken())
                   .setName(llmDto.getName())
                   .setActive(true); // Always activate when updating
                   
        llmRepository.save(existingLlm);
    }

    private void createNewLlm(LlmDto llmDto) {
        log.info("[SETTINGS] Creating new LLM: {}", llmDto.getName());
        
        Llm newLlm = new Llm()
                .setUid(generateUid())
                .setName(llmDto.getName())
                .setAccessToken(llmDto.getAccessToken())
                .setActive(true);
                
        llmRepository.save(newLlm);
    }

    private String generateUid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public IntegrationFormDto getSettings(String platform) {
        Optional<Vcs> vcsOptional = vcsRepository.findAll().stream().findFirst();
        Optional<Llm> llmOptional = llmRepository.findAll().stream().findFirst();

        IntegrationFormDto integrationFormDto = new IntegrationFormDto();
        vcsOptional.ifPresent(vcs -> {
            integrationFormDto.setGitHubAccessToken(getPlatformValue(platform, vcs.getAccessToken()));
            integrationFormDto.setGitHubRepository(vcs.getRepository());
            integrationFormDto.setGitHubBranch(vcs.getBranch());
        });
        llmOptional.ifPresent(llm -> {
            integrationFormDto.setEliteaToken(getPlatformValue(platform, llm.getAccessToken()));
        });
        return integrationFormDto;
    }

    @Override
    public VcsDto getVcs(String platform) {
        Optional<Vcs> vcsOptional = vcsRepository.findAll().stream().findFirst();

        VcsDto vcsDto = new VcsDto();
        vcsOptional.ifPresent(vcs -> {
            vcsDto.setAccessToken(getPlatformValue(platform, vcs.getAccessToken()));
            vcsDto.setRepository(vcs.getRepository());
            vcsDto.setBranch(vcs.getBranch());
        });
        return vcsDto;
    }

    @Override
    public boolean isAvailableForSd() {
        return vcsRepository.findAll().stream()
                .findFirst()
                .map(this::isVcsConfigurationComplete)
                .orElse(false);
    }

    private boolean isVcsConfigurationComplete(Vcs vcs) {
        return !StringUtils.isEmpty(vcs.getAccessToken()) 
                && !StringUtils.isEmpty(vcs.getRepository()) 
                && !StringUtils.isEmpty(vcs.getBranch());
    }

    @Override
    public LlmDto getLlm(String model) {
        if (isPlatformModel(model)) {
            return getFirstLlmForPlatform(model);
        }
        return getLlmByName(model);
    }

    private boolean isPlatformModel(String model) {
        return "ui".equals(model) || "ai".equals(model);
    }

    private LlmDto getFirstLlmForPlatform(String platform) {
        return llmRepository.findAll().stream()
                .findFirst()
                .map(llm -> createLlmDto(llm, platform, false))
                .orElse(new LlmDto());
    }

    private LlmDto getLlmByName(String modelName) {
        return llmRepository.findAll().stream()
                .filter(llm -> llm.getName().equals(modelName))
                .findFirst()
                .map(llm -> createLlmDto(llm, modelName, true))
                .orElse(new LlmDto());
    }

    private LlmDto createLlmDto(Llm llm, String platform, boolean includeId) {
        LlmDto llmDto = new LlmDto()
                .setAccessToken(getPlatformValue(platform, llm.getAccessToken()))
                .setName(llm.getName())
                .setActive(llm.isActive());
                
        if (includeId) {
            llmDto.setId(llm.getUid());
        }
        
        return llmDto;
    }

    @Override
    public List<LlmDto> getLlmAll() {
        return getAllLlms();
    }

    @Override
    public List<LlmDto> setActiveLlm(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("[SETTINGS] Invalid LLM ID provided for activation");
            return getAllLlms();
        }

        Optional<Llm> llmOptional = llmRepository.findById(id);
        if (llmOptional.isPresent()) {
            activateLlm(llmOptional.get());
        } else {
            log.warn("[SETTINGS] LLM with ID {} not found", id);
        }
        
        return getAllLlms();
    }

    private void activateLlm(Llm llm) {
        log.info("[SETTINGS] Activating LLM: {}", llm.getName());
        deactivateAllModels();
        llm.setActive(true);
        llmRepository.save(llm);
    }

    @Override
    public LlmDto getActiveLlm() {
        Optional<LlmDto> llmOptional = llmRepository.findAll().stream()
                .filter(Llm::isActive)
                .map(llm ->
                        new LlmDto()
                                .setId(llm.getUid())
                                .setName(llm.getName())
                                .setAccessToken(llm.getAccessToken())
                                .setActive(llm.isActive())
                                .setCreatedDate(llm.getCreatedDate()))
                .findFirst();
        return llmOptional.orElse(new LlmDto());
    }

    @Override
    public void deleteLlm(String id) {
        log.info("[ELITEA] Delete Llm configuration by id: " + id);
        llmRepository.deleteById(id);
    }

    private @NotNull List<LlmDto> getAllLlms() {
        return llmRepository.findAll().stream()
                .map(llm ->
                        new LlmDto()
                                .setId(llm.getUid())
                                .setName(llm.getName())
                                .setAccessToken(llm.getAccessToken())
                                .setActive(llm.isActive())
                                .setCreatedDate(llm.getCreatedDate()))
                .sorted(Comparator.comparing(LlmDto::getCreatedDate))
                .toList();
    }

    private void deactivateAllModels() {
        log.info("[SETTINGS] Deactivating all LLM models");
        List<Llm> allLlms = llmRepository.findAll();
        allLlms.forEach(llm -> llm.setActive(false));
        llmRepository.saveAll(allLlms);
    }

    private String getPlatformValue(String platform, String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        
        if (!"ai".equals(platform) && value.length() > 6) {
            String prefix = value.substring(0, 3);
            String suffix = value.substring(value.length() - 3);
            return prefix + "*****" + suffix;
        }
        
        return value;
    }
}
