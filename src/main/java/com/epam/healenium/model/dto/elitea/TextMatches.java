package com.epam.healenium.model.dto.elitea;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TextMatches implements Serializable {
    private String fragment;
    private String object_url;
    private List<Object> matches;
}
