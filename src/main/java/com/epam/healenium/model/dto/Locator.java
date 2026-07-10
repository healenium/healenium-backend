package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Locator {
    private String value;
    private String type;
}
