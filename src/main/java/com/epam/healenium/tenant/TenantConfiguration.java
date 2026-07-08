package com.epam.healenium.tenant;

import com.epam.healenium.tenant.ai.TenantValidationService;
import com.epam.healenium.tenant.membership.MembershipResolutionService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

/**
 * Registers {@link TenantFilter} only for Pro edition.
 */
@Configuration
@Profile("pro")
public class TenantConfiguration {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(
            TenantValidationService tenantValidationService,
            MembershipResolutionService membershipResolutionService) {
        FilterRegistrationBean<TenantFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TenantFilter(tenantValidationService, membershipResolutionService));
        // After Spring Security (default order -100) so Jwt is available when we branch on Bearer vs WebDriver.
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
