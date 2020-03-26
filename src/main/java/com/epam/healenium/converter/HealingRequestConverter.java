package com.epam.healenium.converter;

import com.epam.healenium.model.dto.HealingRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealingRequestConverter implements Converter<String, HealingRequestDto> {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public HealingRequestDto convert(String source) {
        return objectMapper.readValue(source, HealingRequestDto.class);
    }
}
