package com.epam.healenium.config;

import org.hibernate.cfg.MappingSettings;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class HibernateJsonConfiguration {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperPropertiesCustomizer(JsonMapper jsonMapper) {
        Jackson3JsonFormatMapper formatMapper = new Jackson3JsonFormatMapper(jsonMapper);
        return properties -> properties.put(MappingSettings.JSON_FORMAT_MAPPER, formatMapper);
    }
}
