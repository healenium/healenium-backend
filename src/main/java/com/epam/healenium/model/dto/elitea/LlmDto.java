package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class LlmDto {
    private String id;
    
    @NotBlank(message = "LLM name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "LLM name contains invalid characters")
    private String name;
    
    @NotBlank(message = "Access token cannot be blank")
    private String accessToken;
    
    private boolean isActive;
    private LocalDateTime createdDate;
}
