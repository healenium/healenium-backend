package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;
import io.prometheus.client.Summary;

import java.util.Map;

public interface PrometheusService {
    void pushUnsuccessHealingResult(HealingResult healingResult);

    void deleteSuccessHealingResult(HealingResult healingResult);

    void pushAndClear(Map<String, String> groupingKeys);

    void deleteAndClear(Map<String, String> groupingKeys);

    Summary createSummaryLatency();
}
