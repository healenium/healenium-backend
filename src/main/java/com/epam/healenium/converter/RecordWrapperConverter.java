package com.epam.healenium.converter;

import com.epam.healenium.model.wrapper.RecordWrapper;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class RecordWrapperConverter implements AttributeConverter<RecordWrapper, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(RecordWrapper recordWrapper) {
        String recordWrapperJson = null;
        try {
            recordWrapperJson = objectMapper.writeValueAsString(recordWrapper);
        } catch (final JacksonException e) {
            log.error("JSON writing error", e);
        }

        return recordWrapperJson;
    }

    @Override
    public RecordWrapper convertToEntityAttribute(String recordJSON) {
        RecordWrapper recordWrapper = null;
        try {
            recordWrapper = objectMapper.readValue(recordJSON, RecordWrapper.class);
        } catch (final JacksonException e) {
            log.error("JSON reading error", e);
        }

        return recordWrapper;
    }
}
