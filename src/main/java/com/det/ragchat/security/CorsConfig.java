package com.det.ragchat.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.det.ragchat.config.AppProperties;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private final AppProperties props;

    public CorsConfig(AppProperties props) {
        this.props = props;
    }

    @Bean
    public CorsFilter corsFilter() {
        if (!props.getCors().isEnabled()) {
            // Empty config = no CORS headers
            return new CorsFilter(new UrlBasedCorsConfigurationSource());
        }

        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(false);
        cfg.setAllowedOrigins(split(props.getCors().getAllowedOrigins()));
        cfg.setAllowedMethods(split(props.getCors().getAllowedMethods()));
        cfg.setAllowedHeaders(split(props.getCors().getAllowedHeaders()));
        cfg.setExposedHeaders(List.of("X-Request-Id", "X-RateLimit-Remaining"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }

    private List<String> split(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
