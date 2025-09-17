package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class LocatorPathsDto {
    @NotBlank(message = "ID cannot be blank")
    private String id;
    
    private String locator;
    private String locatorType;
    
    @NotBlank(message = "Selected path cannot be blank")
    private String selectedPath;
}
