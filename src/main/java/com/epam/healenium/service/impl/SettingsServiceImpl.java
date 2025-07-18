package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.Llm;
import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.dto.elitea.IntegrationFormDto;
import com.epam.healenium.model.dto.elitea.LlmDto;
import com.epam.healenium.model.dto.elitea.VcsDto;
import com.epam.healenium.repository.LlmRepository;
import com.epam.healenium.repository.VcsRepository;
import com.epam.healenium.service.SettingsService;
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
public class SettingsServiceImpl implements SettingsService {

    private final VcsRepository vcsRepository;
    private final LlmRepository llmRepository;

    @Override
    public void saveOrUpdateVcs(VcsDto vcsDto) {
        if (vcsDto.getRepository() != null || vcsDto.getAccessToken() != null
                || vcsDto.getBranch() != null) {
            Optional<Vcs> existingGitHubCredential = vcsRepository.findAll().stream().findFirst();

            if (existingGitHubCredential.isPresent()) {
                Vcs vcs = existingGitHubCredential.get();
                if (vcsDto.getAccessToken() != null) {
                    vcs.setAccessToken(vcsDto.getAccessToken());
                }
                if (vcsDto.getRepository() != null) {
                    vcs.setRepository(vcsDto.getRepository());
                }
                if (vcsDto.getBranch() != null) {
                    vcs.setBranch(vcsDto.getBranch());
                }
                vcsRepository.save(vcs);
            } else {
                Vcs newVcs = new Vcs()
                        .setUid(generateUid())
                        .setName("github")
                        .setRepository(vcsDto.getRepository())
                        .setBranch(vcsDto.getBranch())
                        .setAccessToken(vcsDto.getAccessToken());
                vcsRepository.save(newVcs);
            }
        }
    }

    @Override
    public void saveOrUpdateLlm(LlmDto llmDto) {
        if (llmDto.getAccessToken() != null) {
            Optional<Llm> vcsOptional = llmRepository.findAll().stream().findFirst();
            if (vcsOptional.isPresent()) {
                Llm existingLlm = vcsOptional.get();
                existingLlm.setAccessToken(llmDto.getAccessToken());
                llmRepository.save(existingLlm);
            } else {
                Llm newLlm = new Llm()
                        .setUid(generateUid())
                        .setName("elitea")
                        .setAccessToken(llmDto.getAccessToken());
                llmRepository.save(newLlm);
            }
        }
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
    public LlmDto getLlm(String platform) {
        Optional<Llm> llmOptional = llmRepository.findAll().stream().findFirst();

        LlmDto llmDto = new LlmDto();
        llmOptional.ifPresent(llm -> {
            llmDto.setAccessToken(getPlatformValue(platform, llm.getAccessToken()));
        });
        return llmDto;
    }

    public String getPlatformValue(String platform, String value) {
        if ("ui".equals(platform) && !StringUtils.isEmpty(value)) {
            String prefix = value.substring(0, 3);
            String suffix = value.substring(value.length() - 3);
            return prefix + "*****" + suffix;
        }
        return value;
    }
}
