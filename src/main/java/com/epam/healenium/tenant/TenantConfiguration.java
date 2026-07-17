package com.epam.healenium.tenant;

import com.epam.healenium.tenant.m2m.M2mAuthFilter;
import com.epam.healenium.tenant.m2m.M2mAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

/**
 * Registers Pro tenant and M2M filters.
 */
@Configuration
@Profile("pro")
@EnableConfigurationProperties(M2mAuthProperties.class)
public class TenantConfiguration {

    @Bean
    public FilterRegistrationBean<M2mAuthFilter> m2mAuthFilterRegistration(M2mAuthProperties properties) {
        FilterRegistrationBean<M2mAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new M2mAuthFilter(properties));
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 20);
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(
            TenantValidationService tenantValidationService) {
        FilterRegistrationBean<TenantFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TenantFilter(tenantValidationService));
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
