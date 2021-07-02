package com.epam.healenium.config;

import com.epam.healenium.converter.NodeDeserializer;
import com.epam.healenium.converter.NodeSerializer;
import com.epam.healenium.treecomparing.Node;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule("node")
                .addSerializer(Node.class, new NodeSerializer())
                .addDeserializer(Node.class, new NodeDeserializer());
        return new ObjectMapper().registerModule(module)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

}