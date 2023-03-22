package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SelectorDto {
    private String id;
    private String locator;
    private boolean enableHealing;
}
