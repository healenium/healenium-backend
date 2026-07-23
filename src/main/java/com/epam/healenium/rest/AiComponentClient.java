package com.epam.healenium.rest;

import com.epam.healenium.config.AiProperties;
import com.epam.healenium.model.dto.XPathResponse;
import com.epam.healenium.treecomparing.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

/**
 * Outbound client for healenium-ai endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AiProperties.class)
public class AiComponentClient {

    private static final String XPATH_PATH = "/healenium-ai/selectors/xpath";
    private static final String SERVICE_KEY_HEADER = "Healenium-AI-Service-Key";

    private final WebClient.Builder webClientBuilder;
    private final AiProperties aiProperties;
    private final JsonMapper jsonMapper;

    /**
     * Asks healenium-ai to build an XPath for the given node.
     * Soft-fails on errors/timeouts so healing can continue.
     */
    public Optional<String> generateXPath(Node node) {
        if (node == null) {
            return Optional.empty();
        }
        try {
            byte[] body = jsonMapper.writeValueAsBytes(node);
            WebClient.RequestBodySpec request = webClientBuilder
                    .baseUrl(aiProperties.getBaseUrl())
                    .build()
                    .post()
                    .uri(XPATH_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (StringUtils.hasText(aiProperties.getServiceKey())) {
                request.header(SERVICE_KEY_HEADER, aiProperties.getServiceKey());
            }

            XPathResponse response = request
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(XPathResponse.class)
                    .timeout(aiProperties.getTimeout())
                    .block();

            if (response == null || !StringUtils.hasText(response.getXpath())) {
                log.warn("AI xpath response empty for node tag={}, id={}", node.getTag(), node.getId());
                return Optional.empty();
            }
            return Optional.of(response.getXpath());
        } catch (WebClientResponseException e) {
            log.warn("AI xpath call failed: {} {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("AI xpath call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
