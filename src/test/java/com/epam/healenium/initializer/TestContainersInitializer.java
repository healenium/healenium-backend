package com.epam.healenium.initializer;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class TestContainersInitializer {

    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER;

    static {
        POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("healenium")
                .withInitScript("init.sql");
        POSTGRES_SQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String jdbcUrl = POSTGRES_SQL_CONTAINER.getJdbcUrl() + "?currentSchema=healenium";
        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> "healenium_app");
        registry.add("spring.datasource.password", () -> "healenium_app_password");
        registry.add("spring.datasource.hikari.schema", () -> "healenium");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "healenium");
        registry.add("spring.liquibase.default-schema", () -> "healenium");
        // Migrations need a privileged user; runtime stays healenium_app (no BYPASSRLS).
        registry.add("spring.liquibase.user", POSTGRES_SQL_CONTAINER::getUsername);
        registry.add("spring.liquibase.password", POSTGRES_SQL_CONTAINER::getPassword);
        registry.add("spring.liquibase.url", () -> jdbcUrl);
    }
}
