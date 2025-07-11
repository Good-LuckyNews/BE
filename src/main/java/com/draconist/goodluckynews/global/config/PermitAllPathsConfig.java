package com.draconist.goodluckynews.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermitAllPathsConfig {

    @Bean
    public String[] permitAllPaths() {
        return new String[]{
                "/api/member/login", "/api/member/join", "/ws/**",
        };
    }
}
