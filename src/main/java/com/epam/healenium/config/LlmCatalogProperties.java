package com.epam.healenium.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("healenium")
@Validated
public class LlmCatalogProperties {

    @Valid
    private List<Model> llmModels = new ArrayList<>();

    public List<Model> getLlmModels() {
        return llmModels;
    }

    public void setLlmModels(List<Model> llmModels) {
        this.llmModels = llmModels != null ? llmModels : new ArrayList<>();
    }

    public static class Model {

        @NotBlank
        private String name;

        @NotBlank
        @Pattern(regexp = "openai|anthropic|gemini")
        private String protocol;

        @NotBlank
        @Size(max = 512)
        private String baseUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
