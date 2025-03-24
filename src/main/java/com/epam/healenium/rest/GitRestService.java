package com.epam.healenium.rest;

import com.epam.healenium.model.dto.elitea.GitSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitRestService {

    private static final String API_GITHUB_URL = "https://api.github.com";

    private final WebClient.Builder webClient;

    public GitSearchResponseDto callExternalService(String repoName, String brokenLocatorValue, String authorizationHeader) {
        String queryUri = String.format("/search/code?q=repo:%s+%s", repoName, brokenLocatorValue);
        log.info("[ELITEA] Calling search API for GitHub Repository: {} and value: {}", repoName, brokenLocatorValue);

        try {
            return webClient
                    .baseUrl(API_GITHUB_URL)
                    .build()
                    .get()
                    .uri(queryUri)
                    .header("Authorization", authorizationHeader)
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
}
