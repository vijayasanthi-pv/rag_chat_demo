package com.example.ragchat.security;

import com.example.ragchat.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Ensure a consistent role prefix for SpEL expressions like hasRole/hasAnyRole.
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("ROLE_");
    }


    /**
     * Public endpoints (Swagger + OpenAPI + health/info).
     * Health/info must be public so Docker healthcheck works even in JWT mode.
     */
    @Bean
    @Order(1)
    SecurityFilterChain publicChain(HttpSecurity http, RequestCorrelationFilter requestCorrelationFilter, RequestLoggingFilter requestLoggingFilter) throws Exception {
        http.securityMatcher(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health",
                "/actuator/info"
        );

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(requestLoggingFilter, RequestCorrelationFilter.class);
        return http.build();
    }

    /**
     * Protected APIs.
     * - API_KEY mode: ApiKeyAuthFilter enforces X-API-Key (and user header)
     * - JWT mode: oauth2ResourceServer(jwt) enforces Bearer token
     */
    @Bean
    @Order(2)
    SecurityFilterChain protectedChain(
            HttpSecurity http,
            AppProperties props,
            ApiKeyAuthFilter apiKeyAuthFilter,
            RateLimitFilter rateLimitFilter,
            RequestCorrelationFilter requestCorrelationFilter,
            RequestLoggingFilter requestLoggingFilter
    ) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Enable CORS via Spring Security (uses your CorsConfigurationSource bean if you have one)
        http.cors(Customizer.withDefaults());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
        );

        // Enable JWT only when mode is JWT (requires issuer-uri or jwk-set-uri env var)
        if (props.getSecurity().getMode() == AppProperties.AuthMode.JWT) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        }

        // Order: correlation -> apiKey -> rateLimit
        http.addFilterBefore(requestCorrelationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(requestLoggingFilter, RequestCorrelationFilter.class);
        http.addFilterAfter(apiKeyAuthFilter, RequestLoggingFilter.class);
        http.addFilterAfter(rateLimitFilter, ApiKeyAuthFilter.class);

        return http.build();
    }
}
