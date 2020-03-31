package com.epam.healenium.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class Locator {

    private final String value;
    private final String type;

    @JsonCreator
    public Locator(@JsonProperty("value") String value, @JsonProperty("type") String type) {
        this.value = value;
        this.type = type;
    }
}
