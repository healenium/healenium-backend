package com.epam.healenium.service;

import com.epam.healenium.model.domain.HealingResult;
import io.prometheus.client.Summary;

import java.util.Map;

public interface PrometheusService {
    void pushUnseccessHealingResult(HealingResult healingResult);

    void pushAndClear(Map<String, String> groupingKeys);

    Summary createSummaryLatency();
}
