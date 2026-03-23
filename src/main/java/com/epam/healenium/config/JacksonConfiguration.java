package com.epam.healenium.config;

import com.epam.healenium.converter.NodeDeserializer;
import com.epam.healenium.converter.NodeSerializer;
import com.epam.healenium.treecomparing.Node;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/** Hooks {@link Node} serde into Boot 4's {@link JsonMapper} (used by MVC), not a standalone {@code ObjectMapper} bean. */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class JacksonConfiguration implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder builder) {
        SimpleModule nodeModule = new SimpleModule("node")
                .addSerializer(Node.class, new NodeSerializer())
                .addDeserializer(Node.class, new NodeDeserializer());
        builder.addModule(nodeModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

}
