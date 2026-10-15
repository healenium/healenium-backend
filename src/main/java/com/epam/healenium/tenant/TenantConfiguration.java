package com.epam.healenium.tenant;

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
public class TenantConfiguration implements WebMvcConfigurer {

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
