package com.epam.healenium.initializer;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = TestContainersInitializer.Initializer.class)
@Testcontainers
public abstract class TestContainersInitializer {

    @Container
    public static GenericContainer<?> mongoContainer = new GenericContainer<>("mongo:latest")
        .withExposedPorts(27017)
        .withCommand("--replSet rs0 --bind_ip_all");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            try {
                mongoContainer.execInContainer("/bin/bash", "-c", "mongo --eval 'rs.initiate()' --quiet");
                TestPropertyValues.of(
                    "spring.data.mongodb.host="+ mongoContainer.getContainerIpAddress(),
                    "spring.data.mongodb.port="+ mongoContainer.getMappedPort(27017))
                    .applyTo(configurableApplicationContext.getEnvironment());
            } catch (IOException | InterruptedException e) {
                log.error("Failed to initialize context", e);
            }
        }
    }
}
