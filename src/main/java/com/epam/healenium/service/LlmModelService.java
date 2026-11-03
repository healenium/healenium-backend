package com.epam.healenium.service;

import com.epam.healenium.config.LlmCatalogProperties;
import com.epam.healenium.model.dto.LlmModelDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@EnableConfigurationProperties(LlmCatalogProperties.class)
public class LlmModelService {

    private final List<LlmModelDto> models;

    public LlmModelService(LlmCatalogProperties catalog) {
        this.models = List.copyOf(load(catalog));
    }

    public List<LlmModelDto> list() {
        return models;
    }

    private static List<LlmModelDto> load(LlmCatalogProperties catalog) {
        List<LlmModelDto> models = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (LlmCatalogProperties.Model entry : catalog.getLlmModels()) {
            if (validEntry(entry, names)) {
                models.add(new LlmModelDto()
                        .setModel(entry.getModel())
                        .setProtocol(entry.getProtocol())
                        .setBaseUrl(entry.getBaseUrl()));
            }
        }
        models.sort(Comparator.comparing(LlmModelDto::getModel, Comparator.nullsLast(String::compareTo)));
        return models;
    }

    private static boolean validEntry(LlmCatalogProperties.Model entry, Set<String> names) {
        if (!isHttpUrl(entry.getBaseUrl())) {
            log.warn("Skipping LLM model catalog entry with an invalid base URL: {}, {}",
                    entry.getModel(), entry.getBaseUrl());
            return false;
        }
        if (!names.add(entry.getModel())) {
            log.warn("Skipping duplicate LLM model catalog entry: {}", entry.getModel());
            return false;
        }
        return true;
    }

    private static boolean isHttpUrl(String value) {
        if (value == null) {
            return false;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (java.net.URISyntaxException ex) {
            return false;
        }
    }
}
