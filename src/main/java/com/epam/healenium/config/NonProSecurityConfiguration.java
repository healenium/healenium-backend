package com.epam.healenium.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Free / test: no JWT resource server; keeps Spring Security from applying the default deny-all chain.
 */
@Configuration
@EnableWebSecurity
@Profile("!pro")
public class NonProSecurityConfiguration {

    @Bean
    public SecurityFilterChain nonProSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
