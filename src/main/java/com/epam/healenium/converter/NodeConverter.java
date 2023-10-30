package com.epam.healenium.converter;

import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
@Slf4j
public class NodeConverter implements AttributeConverter<NodePathWrapper, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(NodePathWrapper nodePathWrapper) {

        String nodePathWrapperJson = null;
        try {
            nodePathWrapperJson = objectMapper.writeValueAsString(nodePathWrapper);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return nodePathWrapperJson;
    }

    @Override
    public NodePathWrapper convertToEntityAttribute(String nodePathWrapperJson) {

        NodePathWrapper nodePathWrapper = null;
        try {
            nodePathWrapper = objectMapper.readValue(nodePathWrapperJson, NodePathWrapper.class);
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }

        return nodePathWrapper;
    }

}
