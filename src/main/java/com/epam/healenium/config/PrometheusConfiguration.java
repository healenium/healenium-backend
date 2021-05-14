package com.epam.healenium.config;

import com.epam.healenium.util.Utils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory;
import io.prometheus.client.exporter.PushGateway;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

import static com.epam.healenium.constants.Constants.PROMETHEUS_JOB_NAME;

/**
 * @author rdanilov
 * @since 04.01.2020
 */
@Configuration
public class PrometheusConfiguration {

    @Value("${app.prometheus.url}")
    private String prometheusURL;

    @Bean
    @Scope(value = "prototype")
    @SneakyThrows
    public PrometheusPushGatewayManager prometheusPushGatewayManager(Map<String, String> groupingKeys,
                                                                     PrometheusPushGatewayManager.ShutdownOperation method) {
        PushGateway pg = new PushGateway(new URL(prometheusURL));
        pg.setConnectionFactory(new BasicAuthHttpConnectionFactory(
                Utils.getKeyFromKeyStore("loginAlias", "uSi51JkQTJlgi", "keystore/prometrics.ks"),
                Utils.getKeyFromKeyStore("passwordAlias", "uSi51JkQTJlgi", "keystore/prometrics.ks")));
        return new PrometheusPushGatewayManager(pg, CollectorRegistry.defaultRegistry, Duration.ofSeconds(1L),
                PROMETHEUS_JOB_NAME, groupingKeys, method);
    }
}
