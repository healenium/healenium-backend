package com.epam.healenium.rest;

import com.epam.healenium.model.domain.HealingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.format.DateTimeFormatter;

import static com.epam.healenium.constants.Constants.YYYY_MM_DD;


@Slf4j
@Service
public class AmazonRestService {

    private static final String AWS_LAMBDA_API_URL = "https://b72wq5tyt0.execute-api.eu-central-1.amazonaws.com/healenium";

    public void uploadMetrics(String metrics, HealingResult selectedResult, String hostProject, String url) {
        WebClient.builder()
                .baseUrl(AWS_LAMBDA_API_URL + "/upload")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("project", hostProject)
                        .queryParam("result", selectedResult.getId().toString())
                        .queryParam("healing", selectedResult.getHealing().getUid())
                        .queryParam("url", url)
                        .build())
                .bodyValue(metrics)
                .exchange()
                .subscribe();
    }

    public void moveMetrics(String sourceBucketName, HealingResult healingResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        WebClient.builder()
                .baseUrl(AWS_LAMBDA_API_URL + "/move")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("source", sourceBucketName)
                        .queryParam("create", healingResult.getCreateDate().format(formatter))
                        .queryParam("result", healingResult.getId().toString())
                        .queryParam("healing", healingResult.getHealing().getUid())
                        .build())
                .exchange()
                .subscribe();
    }
}
