package com.epam.healenium.service.impl;

import com.epam.healenium.config.PrometheusConfiguration;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.service.PrometheusService;
import com.epam.healenium.util.Utils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.healenium.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PrometheusServiceImpl implements PrometheusService {

    private final PrometheusConfiguration prometheusConfiguration;

    @Override
    public void pushUnsuccessHealingResult(HealingResult healingResult) {
        createSummaryDOM(healingResult);
        pushAndClear(getHealingIdAsGroupingKeys(healingResult));
    }

    @Override
    public void deleteSuccessHealingResult(HealingResult healingResult) {
        deleteAndClear(getHealingIdAsGroupingKeys(healingResult));
    }

    private Map<String, String> getHealingIdAsGroupingKeys(HealingResult healingResult) {
        Map<String, String> groupingKeys = new HashMap<>();
        groupingKeys.put(HEALING_ID, String.valueOf(healingResult.getHealing().getUid()));
        return groupingKeys;
    }

    @Override
    public void pushAndClear(Map<String, String> groupingKeys) {
        prometheusConfiguration
                .prometheusPushGatewayManager(getFilteredPrometheusGroupingKeys(groupingKeys),
                        PrometheusPushGatewayManager.ShutdownOperation.PUSH)
                .shutdown();
        CollectorRegistry.defaultRegistry.clear();
    }

    @Override
    public void deleteAndClear(Map<String, String> groupingKeys) {
        prometheusConfiguration
                .prometheusPushGatewayManager(getFilteredPrometheusGroupingKeys(groupingKeys),
                        PrometheusPushGatewayManager.ShutdownOperation.DELETE)
                .shutdown();
        CollectorRegistry.defaultRegistry.clear();
    }

    @Override
    public Summary createSummaryLatency() {
        return Summary.build()
                .name("healing_latency")
                .help("Duration in seconds of healing")
                .register();
    }

    private void createSummaryDOM(HealingResult healingResult) {
        Summary.build()
                .name("time_" + Utils.getCurrentDate(HH_MM_SS_DD_MMM_YYYY))
                .help(healingResult.getHealing().getPageContent() + " UnsuccessfulHealingResult: " + healingResult.getLocator().toString())
                .register();
    }

    private Map<String, String> getFilteredPrometheusGroupingKeys(Map<String, String> groupingKeys) {
        return groupingKeys.entrySet().stream()
                .filter(map -> !map.getValue().contains("/"))
                .filter(map -> SESSION_KEY.equals(map.getKey())
                        || HOST_PROJECT.equals(map.getKey())
                        || HEALING_ID.equals(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
