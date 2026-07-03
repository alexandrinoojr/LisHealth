package com.integracaolab.app.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ApiKeyFilter apiKeyFilter) throws Exception {

        http
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable())

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )

            .addFilterBefore(
                apiKeyFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
