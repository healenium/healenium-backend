package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class EliteaHealing {
    private EliteaLocator brokenLocator;
    private EliteaLocator healedLocator;
    private String filePath;
}
