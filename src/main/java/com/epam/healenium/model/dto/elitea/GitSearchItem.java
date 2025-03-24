package com.epam.healenium.model.dto.elitea;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GitSearchItem {
    private String path;
    private List<TextMatches> text_matches;
}
