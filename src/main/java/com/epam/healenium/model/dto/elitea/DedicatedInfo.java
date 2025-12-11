package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DedicatedInfo {
    private boolean availableForSd;
    private boolean availableForMr;
}
