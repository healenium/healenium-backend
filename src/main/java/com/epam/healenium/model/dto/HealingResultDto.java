package com.epam.healenium.model.dto;

import com.epam.healenium.model.Locator;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@Data
public class HealingResultDto {

    private Locator locator;
    private Double score;
    private boolean successHealing;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createDate;

}
