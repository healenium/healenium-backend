package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SelectorCandidate {
    List<ElementCandidate> elementCandidates;

    // -- to save healing result --
    // one List<Node> from ReferenceElementsDto::List<List<Node>> paths
    // in playwright usually there is only one List<Node>
    @ToString.Exclude
    List<Node> path;
    @ToString.Exclude
    private List<Scored<Node>> scores;
    @ToString.Exclude
    List<HealingCandidateMetaData> candidateMetaData;
}
