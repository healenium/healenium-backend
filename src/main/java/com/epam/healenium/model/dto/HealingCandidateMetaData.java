package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * healenium-web :: HealingCandidateDto
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class HealingCandidateMetaData {

    private Double score;
    private Integer LCSDistance;
    private Integer curPathHeight;
    private Node node;

}
