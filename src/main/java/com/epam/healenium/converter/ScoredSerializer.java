package com.epam.healenium.converter;

import com.epam.healenium.treecomparing.Scored;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class ScoredSerializer extends StdSerializer<Scored> {

    public ScoredSerializer() {
        super(Scored.class);
    }

    @Override
    public void serialize(Scored value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        gen.writeStartObject();
        gen.writeNumberProperty("score", value.getScore());
        gen.writePOJOProperty("value", value.getValue());
        gen.writeEndObject();
    }
}
