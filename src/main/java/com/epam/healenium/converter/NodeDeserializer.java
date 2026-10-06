package com.epam.healenium.converter;

import com.epam.healenium.constants.FieldName;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.NodeBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializes a {@link Node} and restores the ancestor chain from nested {@code parent} objects.
 * Returns the leaf node with {@code parent} links set (via {@link NodeBuilder#addChild}).
 */
@SuppressWarnings("unchecked")
public class NodeDeserializer extends StdDeserializer<Node> {

    public NodeDeserializer() {
        super(Node.class);
    }

    @Override
    public Node deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        JsonNode tree = ctxt.readTree(parser);
        return deserializeLeafWithAncestors(tree, ctxt);
    }

    private Node deserializeLeafWithAncestors(JsonNode leafTree, DeserializationContext ctxt) throws JacksonException {
        List<JsonNode> rootToLeaf = new ArrayList<>();
        for (JsonNode current = leafTree; current != null && !current.isNull() && !current.isMissingNode();
             current = current.get(FieldName.PARENT)) {
            rootToLeaf.add(current);
        }
        Collections.reverse(rootToLeaf);

        // Build root → leaf so NodeBuilder#build sets parent links on children.
        Node child = null;
        for (int i = rootToLeaf.size() - 1; i >= 0; i--) {
            NodeBuilder builder = toBuilder(rootToLeaf.get(i), ctxt);
            if (child != null) {
                builder.addChild(child);
            }
            child = builder.build();
        }

        Node leaf = child;
        while (leaf != null && leaf.getChildren() != null && !leaf.getChildren().isEmpty()) {
            leaf = leaf.getChildren().getFirst();
        }
        return leaf;
    }

    private NodeBuilder toBuilder(JsonNode tree, DeserializationContext ctxt) throws JacksonException {
        String tag = ctxt.readTreeAsValue(tree.path(FieldName.TAG), String.class);
        Integer index = ctxt.readTreeAsValue(tree.path(FieldName.INDEX), Integer.class);
        String innerText = ctxt.readTreeAsValue(tree.path(FieldName.INNER_TEXT), String.class);
        String id = ctxt.readTreeAsValue(tree.path(FieldName.ID), String.class);
        String classes = ctxt.readTreeAsValue(tree.path(FieldName.CLASSES), String.class);
        Map<String, String> attributes = ctxt.readTreeAsValue(tree.path(FieldName.OTHER), Map.class);
        if (attributes == null) {
            attributes = new HashMap<>();
        } else {
            attributes = new HashMap<>(attributes);
        }
        attributes.put(FieldName.ID, id != null ? id : "");
        attributes.put(FieldName.CLASS, classes != null ? classes : "");

        NodeBuilder builder = new NodeBuilder()
                .setTag(tag)
                .setIndex(index != null ? index : 0)
                .setAttributes(attributes);
        if (innerText != null) {
            builder.addContent(innerText);
        }
        return builder;
    }

}
