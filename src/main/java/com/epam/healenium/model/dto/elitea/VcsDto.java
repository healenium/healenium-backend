package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Accessors(chain = true)
public class VcsDto {
    private String name;
    
    @NotBlank(message = "Access token cannot be blank")
    private String accessToken;
    
    @NotBlank(message = "Repository cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._/-]+$", message = "Repository name contains invalid characters")
    private String repository;
    
    @NotBlank(message = "Branch cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._/-]+$", message = "Branch name contains invalid characters")
    private String branch;
}

