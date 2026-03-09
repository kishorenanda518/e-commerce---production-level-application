package com.ecommerce.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AppConfig {

    /**
     * BCryptPasswordEncoder is available via spring-boot-starter-web
     * (pulled in by spring-security-crypto which is a transitive dep).
     *
     * We do NOT need spring-boot-starter-security for this bean alone.
     * No filter chains, no JWT, no authentication manager — just the encoder.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // strength 12 — secure default
    }
}