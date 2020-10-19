package com.epam.healenium.model.wrapper;

import com.epam.healenium.treecomparing.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodePathWrapper {

    private List<Node> nodePath;

}
