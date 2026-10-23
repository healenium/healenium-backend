package com.epam.healenium.service;

import com.epam.healenium.model.domain.LlmModel;
import com.epam.healenium.model.dto.LlmModelDto;
import com.epam.healenium.repository.LlmModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LlmModelService {

    private final LlmModelRepository llmModelRepository;

    public LlmModelDto create(LlmModelDto dto) {
        validate(dto);
        if (llmModelRepository.findByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("LLM model name already exists");
        }
        return toDto(llmModelRepository.save(newModel(dto)));
    }

    public List<LlmModelDto> list() {
        return llmModelRepository.findAll().stream()
                .sorted(Comparator.comparing(LlmModel::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDto)
                .toList();
    }

    public LlmModelDto get(String id) {
        return toDto(find(id));
    }

    public LlmModelDto update(String id, LlmModelDto dto) {
        LlmModel stored = find(id);
        validate(dto);
        if (!stored.getName().equals(dto.getName())) {
            throw new IllegalArgumentException("LLM model name cannot be changed");
        }
        stored.setProtocol(dto.getProtocol());
        stored.setBaseUrl(dto.getBaseUrl());
        return toDto(llmModelRepository.save(stored));
    }

    public void delete(String id) {
        if (!llmModelRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "LLM model not found");
        }
        llmModelRepository.deleteById(id);
    }

    public List<LlmModelDto> importModels(List<LlmModelDto> models) {
        if (models == null) {
            throw new IllegalArgumentException("Import must be a JSON array");
        }
        Set<String> seen = new HashSet<>();
        for (LlmModelDto dto : models) {
            validate(dto);
            if (!seen.add(dto.getName())) {
                throw new IllegalArgumentException("Duplicate LLM model name in import: " + dto.getName());
            }
        }
        for (LlmModelDto dto : models) {
            if (llmModelRepository.findByName(dto.getName()).isEmpty()) {
                llmModelRepository.save(newModel(dto));
            }
        }
        return list();
    }

    private LlmModel find(String id) {
        return llmModelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "LLM model not found"));
    }

    private LlmModel newModel(LlmModelDto dto) {
        return new LlmModel()
                .setUid(UUID.randomUUID().toString())
                .setName(dto.getName())
                .setProtocol(dto.getProtocol())
                .setBaseUrl(dto.getBaseUrl());
    }

    private void validate(LlmModelDto dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("LLM model name cannot be blank");
        }
        if (!dto.getName().matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("LLM model name contains invalid characters");
        }
        if (dto.getProtocol() == null || dto.getProtocol().isBlank()) {
            throw new IllegalArgumentException("Protocol cannot be blank");
        }
        if (!dto.getProtocol().matches("openai|anthropic|gemini")) {
            throw new IllegalArgumentException("Protocol must be openai, anthropic, or gemini");
        }
        if (dto.getBaseUrl() == null || dto.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be blank");
        }
        if (dto.getBaseUrl().length() > 512 || !isHttpUrl(dto.getBaseUrl())) {
            throw new IllegalArgumentException("baseUrl must be an HTTP or HTTPS URL");
        }
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (java.net.URISyntaxException ex) {
            return false;
        }
    }

    private LlmModelDto toDto(LlmModel model) {
        return new LlmModelDto()
                .setId(model.getUid())
                .setName(model.getName())
                .setProtocol(model.getProtocol())
                .setBaseUrl(model.getBaseUrl());
    }
}
