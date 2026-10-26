package com.epam.healenium.service;

import com.epam.healenium.config.LlmCatalogProperties;
import com.epam.healenium.model.dto.LlmModelDto;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            if (entry == null || !isHttpUrl(entry.getBaseUrl())) {
                String name = entry == null ? null : entry.getName();
                throw new IllegalStateException("LLM model catalog entry has an invalid base URL: " + name);
            }
            if (!names.add(entry.getName())) {
                throw new IllegalStateException("Duplicate LLM model name: " + entry.getName());
            }
            models.add(new LlmModelDto()
                    .setName(entry.getName())
                    .setProtocol(entry.getProtocol())
                    .setBaseUrl(entry.getBaseUrl()));
        }
        models.sort(Comparator.comparing(LlmModelDto::getName, Comparator.nullsLast(String::compareTo)));
        return models;
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
