package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
public class HealingDto {

    private String locator;
    private String className;
    private String methodName;
    private Set<HealingResultDto> results;

}
