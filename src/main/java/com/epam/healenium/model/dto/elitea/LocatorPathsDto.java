package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LocatorPathsDto {
    private String id;
    private String locator;
    private String locatorType;
    private String selectedPath;
}
