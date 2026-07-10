package com.epam.healenium.model.dto;

import com.epam.healenium.model.Locator;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ElementCandidate {
    private List<Locator> locators;
    @ToString.Exclude
    private Scored<Node> nodeScored;
}
