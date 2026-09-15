package com.epam.healenium.tenant;

import com.epam.healenium.tenant.m2m.M2mAuthFilter;
import com.epam.healenium.tenant.m2m.M2mAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers Pro tenant filters and MVC interceptors.
 */
@Configuration
@Profile("pro")
@EnableConfigurationProperties(M2mAuthProperties.class)
public class TenantConfiguration implements WebMvcConfigurer {

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

    @Bean
    public TrialReadOnlyInterceptor trialReadOnlyInterceptor() {
        return new TrialReadOnlyInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trialReadOnlyInterceptor())
                .addPathPatterns("/healenium/**")
                .excludePathPatterns("/internal/**", "/actuator/**");
    }
}
