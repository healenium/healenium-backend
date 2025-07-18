package com.epam.healenium.rest;

import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.dto.elitea.GitSearchResponseDto;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class IntegrationRestService {

    private static final String API_GITHUB_URL = "https://api.github.com";
    private static final String ELITEA_URL = "https://nexus.elitea.ai";
    private static final String GET_APPLICATION_DETAILS_URL = "/api/v1/applications/application/prompt_lib/743/";
    private static final String PUT_APPLICATION_DETAILS_URL = "/api/v1/applications/application/prompt_lib/743/";

    private final WebClient.Builder webClient;

    public GitSearchResponseDto callGitHubService(String repoName, String brokenLocatorValue, String authorizationHeader) {
        String queryUri = String.format("/search/code?q=repo:%s+%s", repoName, brokenLocatorValue);
        log.info("[ELITEA] Calling search API for GitHub Repository: {} and value: {}", repoName, brokenLocatorValue);

        try {
            return webClient
                    .baseUrl(API_GITHUB_URL)
                    .build()
                    .get()
                    .uri(queryUri)
                    .header("Authorization", "Bearer " + authorizationHeader)
                    .header("Accept", "application/vnd.github.text-match+json")
                    .retrieve()
                    .bodyToMono(GitSearchResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[ELITEA] GitHub API response error: Status {}, Body {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ELITEA] Error during GitHub search", e);
        }
        return null;
    }

    public String getFile(String repoName, String path) {
        String url = "https://raw.githubusercontent.com/" + repoName + "/master/" + path;
        log.info("[ELITEA] Calling search API for GitHub Repository: {} ", url);

        try {
            return webClient
                    .baseUrl(url)
                    .build()
                    .get()
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[ELITEA] GitHub API response error: Status {}, Body {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[ELITEA] Error during GitHub search", e);
        }
        return null;
    }

    public Mono<ObjectNode> callGetEliteaApplicationDetails(Vcs vcs, String agentId) {
        log.info("[ELITEA] Get Elitea Application Details");

        return webClient
                .baseUrl(ELITEA_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + vcs.getAccessToken())
                .build()
                .get()
                .uri(GET_APPLICATION_DETAILS_URL + agentId)
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .doOnNext(response -> log.debug("[ELITEA] Successfully retrieved application details: {}", response))
                .doOnError(WebClientResponseException.class, e -> log.error("[ELITEA] Get Elitea API response error: Status {}, Body {}",
                        e.getStatusCode(), e.getResponseBodyAsString()))
                .onErrorResume(e -> {
                    log.error("[ELITEA] Error during Get Elitea details", e);
                    return Mono.empty(); // Возвращаем пустой поток при ошибке
                });
    }

    public Mono<ObjectNode> callUpdateEliteaApplicationDetails(Vcs vcs, String agentId, Mono<ObjectNode> modifiedJson) {
        log.info("[ELITEA] Update Elitea Application Details");

        return webClient
                .baseUrl(ELITEA_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + vcs.getAccessToken())
                .build()
                .put()
                .uri(PUT_APPLICATION_DETAILS_URL + agentId)
                .body(modifiedJson, ObjectNode.class)
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .doOnNext(response -> log.debug("[ELITEA] Successfully updated application details: {}", response))
                .doOnError(WebClientResponseException.class, e -> log.error("[ELITEA] Update Elitea API response error: Status {}, Body {}",
                        e.getStatusCode(), e.getResponseBodyAsString()))
                .onErrorResume(e -> {
                    log.error("[ELITEA] Error during Update Elitea application: ", e);
                    return Mono.empty();
                });
    }
}
