package com.epam.healenium.model.dto;

import com.epam.healenium.treecomparing.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectorRequestDto extends RequestDto{

    private String type;
    @ToString.Exclude
    private List<List<Node>> nodePath;

}

