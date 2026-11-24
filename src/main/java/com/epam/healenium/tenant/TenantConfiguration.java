package com.epam.healenium.tenant;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers tenant filter and MVC interceptors.
 */
@Configuration
public class TenantConfiguration implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration() {
        FilterRegistrationBean<TenantFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TenantFilter());
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
