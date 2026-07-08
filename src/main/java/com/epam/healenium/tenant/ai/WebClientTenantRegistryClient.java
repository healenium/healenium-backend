package com.epam.healenium.tenant.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

/**
 * Default implementation that queries healenium-ai service.
 *
 * Expected contract:
 *  - GET {baseUrl}/tenants/{tenantId} -> 200 if exists+active, 404 if not found, 403/410 if disabled
 */
@Component
@RequiredArgsConstructor
public class WebClientTenantRegistryClient implements TenantRegistryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${healenium.ai.base-url:http://localhost:7879}")
    private String baseUrl;

    @Value("${healenium.ai.service-key:}")
    private String serviceKey;

    @Override
    public boolean isTenantActive(UUID tenantId) {
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        try {
            var request = client.get().uri("/tenants/{id}", tenantId);
            if (StringUtils.hasText(serviceKey)) {
                request = request.header("X-Healenium-Service-Key", serviceKey);
            }
            request.retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException e) {
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) {
                return false;
            }
            // For now treat any non-2xx (except 404) as not active/forbidden.
            return false;
        }
    }
}
