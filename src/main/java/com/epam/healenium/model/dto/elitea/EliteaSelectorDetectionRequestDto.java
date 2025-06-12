package com.epam.healenium.model.dto.elitea;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class EliteaSelectorDetectionRequestDto {
    private String id;
    private String locator;
    private String locatorType;
    private List<String> pathList;
}
