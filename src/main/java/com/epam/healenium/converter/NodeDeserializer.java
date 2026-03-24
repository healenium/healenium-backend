package com.epam.healenium.converter;

import com.epam.healenium.constants.FieldName;
import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.NodeBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NodeDeserializer extends StdDeserializer<Node> {

    public NodeDeserializer() {
        super(Node.class);
    }

    @Override
    public Node deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        JsonNode tree = ctxt.readTree(parser);
        String tag = ctxt.readTreeAsValue(tree.path(FieldName.TAG), String.class);
        Integer index = ctxt.readTreeAsValue(tree.path(FieldName.INDEX), Integer.class);
        String innerText = ctxt.readTreeAsValue(tree.path(FieldName.INNER_TEXT), String.class);
        String id = ctxt.readTreeAsValue(tree.path(FieldName.ID), String.class);
        String classes = ctxt.readTreeAsValue(tree.path(FieldName.CLASSES), String.class);
        Map<String, String> attributes = ctxt.readTreeAsValue(tree.path(FieldName.OTHER), Map.class);
        attributes.put(FieldName.ID, id);
        attributes.put(FieldName.CLASS, classes);

        return new NodeBuilder()
                .setTag(tag)
                .setIndex(index)
                .addContent(innerText)
                .setAttributes(attributes)
                .build();
    }

}
