package com.epam.healenium.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for dynamic settings that can be updated at runtime
 */
@Configuration
@Getter
@Setter
public class DynamicSettings {
    
    @Value("${app.selector.key.url-for-key:false}")
    private boolean keySelectorUrl;
    
    @Value("${app.metrics.allow:true}")
    private boolean collectMetrics;
    
    @Value("${app.healing.elements:false}")
    private boolean findElementsAutoHealing;

    @Value("${healenium.score-cap:.6}")
    private double scoreCap;

    // Selector type must be 'cssSelector' or 'xpath'
    @Value("${healenium.selector-type:cssSelector}")
    private String selectorType;

    @Value("${healenium.recovery-tries:1}")
    private int recoveryTries;
}

