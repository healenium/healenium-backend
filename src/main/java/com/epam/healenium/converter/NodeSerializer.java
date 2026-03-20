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
        gen.writeEndObject();
        gen.flush();
    }

}
