package com.epam.healenium.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "healenium.ai")
public class AiProperties {

    /**
     * Base URL of healenium-ai (e.g. http://hlm-ai:6565).
     */
    private String baseUrl = "http://localhost:6565";

    /**
     * Optional shared secret for outbound calls to healenium-ai.
     */
    private String serviceKey = "";

    /**
     * HTTP timeout for AI selector calls.
     */
    private Duration timeout = Duration.ofSeconds(5);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:6565";
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey != null ? serviceKey : "";
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(5);
    }
}
