package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.Llm;
import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.VcsDto;
import com.epam.healenium.repository.LlmRepository;
import com.epam.healenium.repository.VcsRepository;
import com.epam.healenium.service.IntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IntegrationServiceImpl implements IntegrationService {

    public static final String ACCESS_AI = "ai";
    public static final String ACCESS_UI = "ui";
    private final VcsRepository vcsRepository;
    private final LlmRepository llmRepository;

    @Override
    public @NotNull List<VcsDto> createVcs(VcsDto vcsDto) {
        String name = requireText(vcsDto.getName(), "name is required");
        requireText(vcsDto.getAccessToken(), "access token is required");
        String provider = resolveProvider(vcsDto, Optional.empty());
        String baseUrl = resolveBaseUrl(vcsDto, Optional.empty());
        VcsDto.validateProviderAndBaseUrl(provider, baseUrl);
        if (findVcsByName(name).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "VCS configuration name already exists");
        }

        deactivateAllVcs();
        createNewVcs(vcsDto, name, provider, baseUrl);
        return getAllVcs();
    }

    @Override
    public @NotNull List<VcsDto> updateVcs(String id, VcsDto vcsDto) {
        Vcs existingVcs = vcsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VCS configuration not found"));
        String name = requireText(vcsDto.getName(), "name is required");
        findVcsByName(name)
                .filter(other -> !other.getUid().equals(existingVcs.getUid()))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "VCS configuration name already exists");
                });
        String provider = resolveProvider(vcsDto, Optional.of(existingVcs));
        String baseUrl = resolveBaseUrl(vcsDto, Optional.of(existingVcs));
        VcsDto.validateProviderAndBaseUrl(provider, baseUrl);

        deactivateAllVcs();
        existingVcs.setName(name);
        existingVcs.setProvider(provider);
        existingVcs.setBaseUrl(baseUrl);
        if (vcsDto.getRepository() != null) {
            existingVcs.setRepository(vcsDto.getRepository());
        }
        if (vcsDto.getBranch() != null) {
            existingVcs.setBranch(vcsDto.getBranch());
        }
        if (StringUtils.isNotBlank(vcsDto.getAccessToken())) {
            existingVcs.setAccessToken(vcsDto.getAccessToken().trim());
        }
        existingVcs.setActive(true);
        vcsRepository.save(existingVcs);
        return getAllVcs();
    }

    private Optional<Vcs> findVcsByName(String name) {
        return vcsRepository.findAll().stream()
                .filter(vcs -> name.equals(vcs.getName()))
                .findFirst();
    }

    private String requireText(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void createNewVcs(VcsDto vcsDto, String name, String provider, String baseUrl) {
        log.info("[SETTINGS] Creating new VCS configuration");

        Vcs newVcs = new Vcs()
                .setUid(generateUid())
                .setName(name)
                .setProvider(provider)
                .setBaseUrl(baseUrl)
                .setActive(true)
                .setRepository(vcsDto.getRepository())
                .setBranch(vcsDto.getBranch())
                .setAccessToken(vcsDto.getAccessToken().trim());

        vcsRepository.save(newVcs);
    }

    private String resolveProvider(VcsDto vcsDto, Optional<Vcs> existingVcs) {
        if (StringUtils.isNotBlank(vcsDto.getProvider())) {
            return vcsDto.getProvider();
        }
        return existingVcs.map(Vcs::getProvider).filter(StringUtils::isNotBlank).orElse("github");
    }

    private String resolveBaseUrl(VcsDto vcsDto, Optional<Vcs> existingVcs) {
        if (vcsDto.getBaseUrl() != null) {
            return StringUtils.trimToNull(vcsDto.getBaseUrl());
        }
        return existingVcs.map(Vcs::getBaseUrl).orElse(null);
    }

    @Override
    public @NotNull List<LlmDto> createLlm(LlmDto llmDto) {
        if (!isLlmDtoValid(llmDto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid LLM configuration");
        }
        if (StringUtils.isBlank(llmDto.getAccessToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Access token cannot be blank");
        }
        if (findLlmByName(llmDto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "LLM configuration name already exists");
        }

        deactivateAllModels();
        createNewLlm(llmDto);
        return getAllLlms();
    }

    @Override
    public @NotNull List<LlmDto> updateLlm(String id, LlmDto llmDto) {
        if (!isLlmDtoValid(llmDto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid LLM configuration");
        }
        Llm existingLlm = llmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LLM configuration not found"));
        findLlmByName(llmDto.getName())
                .filter(other -> !other.getUid().equals(existingLlm.getUid()))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "LLM configuration name already exists");
                });

        deactivateAllModels();
        updateExistingLlm(existingLlm, llmDto);
        return getAllLlms();
    }

    private boolean isLlmDtoValid(LlmDto llmDto) {
        return llmDto != null
                && StringUtils.isNotBlank(llmDto.getName())
                && StringUtils.isNotBlank(llmDto.getProtocol())
                && llmDto.getProtocol().matches("openai|anthropic|gemini")
                && StringUtils.isNotBlank(llmDto.getBaseUrl())
                && llmDto.getBaseUrl().length() <= 512;
    }

    private Optional<Llm> findLlmByName(String name) {
        return llmRepository.findAll().stream()
                .filter(llm -> llm.getName().equals(name))
                .findFirst();
    }

    private void updateExistingLlm(Llm existingLlm, LlmDto llmDto) {
        log.info("[SETTINGS] Updating existing LLM: {}", existingLlm.getName());
        
        if (StringUtils.isNotBlank(llmDto.getAccessToken())) {
            existingLlm.setAccessToken(llmDto.getAccessToken());
        }
        existingLlm.setName(llmDto.getName())
                   .setProtocol(llmDto.getProtocol())
                   .setBaseUrl(llmDto.getBaseUrl())
                   .setActive(true);

        llmRepository.save(existingLlm);
    }

    private void createNewLlm(LlmDto llmDto) {
        log.info("[SETTINGS] Creating new LLM: {}", llmDto.getName());
        
        Llm newLlm = new Llm()
                .setUid(generateUid())
                .setName(llmDto.getName())
                .setProtocol(llmDto.getProtocol())
                .setBaseUrl(llmDto.getBaseUrl())
                .setAccessToken(llmDto.getAccessToken())
                .setActive(true);
                
        llmRepository.save(newLlm);
    }

    private String generateUid() {
        return UUID.randomUUID().toString();
    }

    @Override
    public VcsDto getActiveVcs() {
        return findActiveVcs()
                .map(vcs -> toVcsDto(vcs, ACCESS_AI))
                .orElse(new VcsDto());
    }

    @Override
    public @NotNull List<VcsDto> getVcsAll() {
        return getAllVcs();
    }

    @Override
    public @NotNull List<VcsDto> activateVcs(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is required");
        }

        Vcs vcs = vcsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VCS configuration not found"));
        deactivateAllVcs();
        vcs.setActive(true);
        vcsRepository.save(vcs);
        return getAllVcs();
    }

    @Override
    public void deleteVcs(String id) {
        if (!vcsRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "VCS configuration not found");
        }
        log.info("[SETTINGS] Delete VCS configuration by id: {}", id);
        vcsRepository.deleteById(id);
    }

    @Override
    public boolean isAvailableForSd() {
        boolean activeVcs = findActiveVcs()
                .map(this::isVcsConfigurationComplete)
                .orElse(false);
        boolean activeLlm = findActiveLlm()
                .map(this::isLlmConfigurationComplete)
                .orElse(false);
        return activeVcs && activeLlm;
    }

    private Optional<Vcs> findActiveVcs() {
        return vcsRepository.findAll().stream()
                .filter(Vcs::isActive)
                .findFirst();
    }

    private @NotNull List<VcsDto> getAllVcs() {
        return vcsRepository.findAll().stream()
                .map(vcs -> toVcsDto(vcs, ACCESS_UI))
                .sorted(Comparator.comparing(VcsDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private VcsDto toVcsDto(Vcs vcs, String accessLevel) {
        return new VcsDto()
                .setId(vcs.getUid())
                .setName(vcs.getName())
                .setProvider(vcs.getProvider())
                .setBaseUrl(vcs.getBaseUrl())
                .setRepository(vcs.getRepository())
                .setBranch(vcs.getBranch())
                .setAccessToken(getPlatformValue(accessLevel, vcs.getAccessToken()))
                .setActive(vcs.isActive())
                .setCreatedDate(vcs.getCreatedDate());
    }

    private void deactivateAllVcs() {
        List<Vcs> allVcs = vcsRepository.findAll();
        allVcs.forEach(vcs -> vcs.setActive(false));
        vcsRepository.saveAll(allVcs);
    }

    private boolean isVcsConfigurationComplete(Vcs vcs) {
        return !StringUtils.isEmpty(vcs.getAccessToken()) 
                && !StringUtils.isEmpty(vcs.getRepository()) 
                && !StringUtils.isEmpty(vcs.getBranch());
    }

    private boolean isLlmConfigurationComplete(Llm llm) {
        return !StringUtils.isEmpty(llm.getAccessToken())
                && !StringUtils.isEmpty(llm.getProtocol())
                && !StringUtils.isEmpty(llm.getBaseUrl())
                && !StringUtils.isEmpty(llm.getName());
    }

    @Override
    public LlmDto getActiveLlm() {
            return findActiveLlm()
                    .map(llm -> createLlmDto(llm, ACCESS_AI, true))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No active LLM configuration"));
    }

    private Optional<Llm> findActiveLlm() {
        return llmRepository.findAll().stream()
                .filter(Llm::isActive)
                .findFirst();
    }

    private LlmDto createLlmDto(Llm llm, String platform, boolean includeId) {
        LlmDto llmDto = new LlmDto()
                .setAccessToken(getPlatformValue(platform, llm.getAccessToken()))
                .setName(llm.getName())
                .setProtocol(llm.getProtocol())
                .setBaseUrl(llm.getBaseUrl())
                .setActive(llm.isActive())
                .setCreatedDate(llm.getCreatedDate());
                
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

        Llm llm = llmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LLM configuration not found"));
        activateLlm(llm);
        return getAllLlms();
    }

    private void activateLlm(Llm llm) {
        log.info("[SETTINGS] Activating LLM: {}", llm.getName());
        deactivateAllModels();
        llm.setActive(true);
        llmRepository.save(llm);
    }

    @Override
    public void deleteLlm(String id) {
        if (!llmRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "LLM configuration not found");
        }
        log.info("[ELITEA] Delete Llm configuration by id: " + id);
        llmRepository.deleteById(id);
    }

    private @NotNull List<LlmDto> getAllLlms() {
        return llmRepository.findAll().stream()
                .map(llm -> createLlmDto(llm, ACCESS_UI, true))
                .sorted(Comparator.comparing(LlmDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void deactivateAllModels() {
        log.info("[SETTINGS] Deactivating all LLM models");
        List<Llm> allLlms = llmRepository.findAll();
        allLlms.forEach(llm -> llm.setActive(false));
        llmRepository.saveAll(allLlms);
    }

    private String getPlatformValue(String accessLevel, String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        
        if (!ACCESS_AI.equals(accessLevel) && value.length() > 6) {
            String prefix = value.substring(0, 3);
            String suffix = value.substring(value.length() - 3);
            return prefix + "*****" + suffix;
        }
        
        return value;
    }
}
