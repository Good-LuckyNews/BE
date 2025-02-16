package com.dragonist.goodluckynews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;

    public SecurityConfig(JWTRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS 설정
        http.cors(cors -> cors.configurationSource(request -> {
            var config = new org.springframework.web.cors.CorsConfiguration();
            config.addAllowedOrigin("*"); // 모든 출처 허용 (원하는 출처로 변경 가능)
            config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
            config.addAllowedHeader("*"); // 모든 헤더 허용
            return config;
        }));

        // CSRF 설정
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(request -> !request.getRequestURI().startsWith("/v1/oauth/login"))); // /v1/oauth/login에 대해서는 CSRF 보호 비활성화

        // 세션 관리 및 JWT 필터 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 필터 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // URL 권한 설정
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/v1/oauth/login").permitAll() // 로그인 경로는 인증 없이 접근 허용
                .anyRequest().authenticated() // 다른 모든 요청은 인증 필요
        );

        return http.build();
    }
}
