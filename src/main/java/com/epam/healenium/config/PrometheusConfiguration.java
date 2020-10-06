package com.epam.healenium.config;

import com.epam.healenium.util.Utils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory;
import io.prometheus.client.exporter.PushGateway;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.Map;

import static com.epam.healenium.constants.Constants.PROMETHEUS_JOB_NAME;
import static com.epam.healenium.constants.Constants.PUSHGATEWAY_SERVER_ADDRESS;

/**
 * @author rdanilov
 * @since 04.01.2020
 */
@Configuration
public class PrometheusConfiguration {

    @Bean
    @Scope(value = "prototype")
    public PrometheusPushGatewayManager prometheusPushGatewayManager(Map<String, String> headers) {
        PushGateway pg = new PushGateway(PUSHGATEWAY_SERVER_ADDRESS);
        pg.setConnectionFactory(new BasicAuthHttpConnectionFactory(
                Utils.getKeyFromKeyStore("loginAlias"),
                Utils.getKeyFromKeyStore("passwordAlias")));
        return new PrometheusPushGatewayManager(pg, CollectorRegistry.defaultRegistry, Duration.ofSeconds(1L),
                PROMETHEUS_JOB_NAME, headers, PrometheusPushGatewayManager.ShutdownOperation.PUSH);
    }
}
