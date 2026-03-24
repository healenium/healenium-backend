package com.epam.healenium.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Servlet-based apps do not get a {@link WebClient.Builder} bean from auto-configuration in Spring Boot 4;
 * outbound WebClient calls still need an explicit bean.
 */
@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
