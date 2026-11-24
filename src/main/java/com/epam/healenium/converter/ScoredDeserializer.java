package com.epam.healenium.converter;

import com.epam.healenium.treecomparing.Node;
import com.epam.healenium.treecomparing.Scored;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class ScoredDeserializer extends StdDeserializer<Scored> {

    public ScoredDeserializer() {
        super(Scored.class);
    }

    @Override
    public Scored deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        JsonNode tree = ctxt.readTree(parser);
        double score = ctxt.readTreeAsValue(tree.path("score"), Double.class);
        Node value = ctxt.readTreeAsValue(tree.path("value"), Node.class);
        return new Scored<>(score, value);
    }
}
