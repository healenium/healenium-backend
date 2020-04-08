package com.epam.healenium.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Created by vsko on 12.04.16.
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class LiquibaseConfiguration {

    private static final String DEFAULT_TEST_SCHEMA = "public";

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return DataSourceBuilder.create(dataSourceProperties.getClassLoader())
            .type(HikariDataSource.class)
            .driverClassName(dataSourceProperties.getDriverClassName())
            .url(dataSourceProperties.getUrl())
            .username(dataSourceProperties.getUsername())
            .password(dataSourceProperties.getPassword())
            .build();
    }
}
