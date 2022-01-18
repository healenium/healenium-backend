package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HealingResultRequestDto {

    private HealingRequestDto requestDto;
    private String metrics;
    private String healingTime;

}
