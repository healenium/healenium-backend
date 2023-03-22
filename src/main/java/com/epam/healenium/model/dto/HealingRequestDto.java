package com.epam.healenium.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealingRequestDto extends RequestDto {

    private String type;
    @ToString.Exclude
    private String pageContent;
    private List<HealingResultDto> results;
    private HealingResultDto usedResult;
    @ToString.Exclude
    private byte[] screenshot;
    @ToString.Exclude
    private String metrics;
    private List<String> elementIds;

}
