package com.epam.healenium.converter;

import com.epam.healenium.treecomparing.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class NodeConverter implements AttributeConverter<List<Node>, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(List<Node> customerInfo) {

        String customerInfoJson = null;
        try {
            customerInfoJson = objectMapper.writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return customerInfoJson;
    }

    @Override
    public List<Node> convertToEntityAttribute(String customerInfoJSON) {

        List<Node> customerInfo = null;
        try {
            customerInfo = objectMapper.readValue(customerInfoJSON, new TypeReference<List<Node>>() {});
        } catch (final IOException e) {
            log.error("JSON reading error", e);
        }

        return customerInfo;
    }

}
