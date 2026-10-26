package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LlmDto {
    private String id;
    
    @NotBlank(message = "LLM name cannot be blank")
    private String name;

    @NotBlank(message = "Protocol cannot be blank")
    @Pattern(regexp = "openai|anthropic|gemini", message = "Protocol must be openai, anthropic, or gemini")
    private String protocol;

    @NotBlank(message = "Base URL cannot be blank")
    @Size(max = 512, message = "Base URL must be at most 512 characters")
    private String baseUrl;

    private String accessToken;
    
    private boolean isActive;
    private LocalDateTime createdDate;
}
