package com.epam.healenium.model.dto;

import com.epam.healenium.model.Locator;
import lombok.Data;
import lombok.ToString;

@Data
public class HealingResultDto {

    private Locator locator;
    private Double score;
    @ToString.Exclude
    private String screenshotName;

}
