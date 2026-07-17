package com.epam.healenium.tenant;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class TenantEditionConfiguration {

    @Configuration
    @Profile("pro")
    static class ProEdition {
        @PostConstruct
        void markPro() {
            TenantEdition.setPro(true);
        }
    }

    @Configuration
    @Profile("!pro")
    static class FreeEdition {
        @PostConstruct
        void markFree() {
            TenantEdition.setPro(false);
        }
    }
}
