package com.epam.healenium.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@ConfigurationProperties("healenium")
@Validated
public class LlmCatalogProperties {

    @Valid
    private List<Model> llmModels = new ArrayList<>();

    public void setLlmModels(List<Model> llmModels) {
        this.llmModels = llmModels != null ? llmModels : new ArrayList<>();
    }

    @Setter
    @Getter
    public static class Model {

        @NotBlank
        private String model;

        @NotBlank
        @Pattern(regexp = "openai|anthropic|gemini")
        private String protocol;

        @NotBlank
        @Size(max = 512)
        private String baseUrl;

    }
}
