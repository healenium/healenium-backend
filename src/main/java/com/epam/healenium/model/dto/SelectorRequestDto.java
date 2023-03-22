package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectorRequestDto extends RequestDto {

    private String id;
    private String type;
    @ToString.Exclude
    private List<List<Node>> nodePath;
    private List<String> elementIds;
    private String sessionId;
    private boolean enableHealing;
}

