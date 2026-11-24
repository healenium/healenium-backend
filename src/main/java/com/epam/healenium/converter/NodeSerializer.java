package com.epam.healenium.converter;

import com.epam.healenium.constants.FieldName;
import com.epam.healenium.treecomparing.Node;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Serializes a {@link Node} including its ancestor chain via nested {@code parent} objects.
 * Children are intentionally omitted to avoid cycles and oversized payloads.
 */
public class NodeSerializer extends StdSerializer<Node> {

    public NodeSerializer() {
        super(Node.class);
    }

    @Override
    public void serializeWithType(Node value, JsonGenerator gen, SerializationContext serializers, TypeSerializer typeSer)
            throws JacksonException {
        WritableTypeId typeId = typeSer.typeId(value, Node.class, JsonToken.START_OBJECT);
        typeSer.writeTypePrefix(gen, serializers, typeId);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, serializers, typeId);
    }

    @Override
    public void serialize(Node value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(value);
        serializeNode(value, gen, visited);
    }

    private void serializeNode(Node value, JsonGenerator gen, Set<Node> visited) throws JacksonException {
        gen.writeStartObject();
        gen.writeStringProperty(FieldName.TAG, value.getTag());
        Integer index = value.getIndex();
        if (index != null) {
            gen.writeNumberProperty(FieldName.INDEX, index);
        } else {
            gen.writeNullProperty(FieldName.INDEX);
        }
        gen.writeStringProperty(FieldName.INNER_TEXT, value.getInnerText());
        gen.writeStringProperty(FieldName.ID, value.getId());
        gen.writeStringProperty(FieldName.CLASSES, String.join(" ", value.getClasses()));
        gen.writePOJOProperty(FieldName.OTHER, value.getOtherAttributes());

        Node parent = value.getParent();
        if (parent != null && visited.add(parent)) {
            gen.writeName(FieldName.PARENT);
            serializeNode(parent, gen, visited);
        }

        gen.writeEndObject();
        gen.flush();
    }

}
