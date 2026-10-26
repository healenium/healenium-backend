package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Accessors(chain = true)
public class LlmModelDto {

    @NotBlank(message = "LLM model name cannot be blank")
    private String name;

    @NotBlank(message = "Protocol cannot be blank")
    @Pattern(regexp = "openai|anthropic|gemini", message = "Protocol must be openai, anthropic, or gemini")
    private String protocol;

    @NotBlank(message = "Base URL cannot be blank")
    @Size(max = 512, message = "Base URL must be at most 512 characters")
    @Pattern(regexp = "^https?://\\S+$", message = "baseUrl must be an HTTP or HTTPS URL")
    private String baseUrl;
}
