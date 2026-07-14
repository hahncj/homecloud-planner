package com.homecloud.planner.common;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Allows the frontend's origin to call the API from a browser. The frontend and
 * backend are served from different origins in every supported setup (Vite dev
 * server on :3000 calling Spring Boot on :8080, or the nginx container calling
 * the backend container), so without this, all browser requests are blocked by
 * the same-origin policy.
 *
 * <p>Exposed as a {@link CorsConfigurationSource} bean (rather than a
 * {@code WebMvcConfigurer}) so Spring Security's {@code .cors()} picks up the
 * exact same configuration — CORS has to be handled once, by whichever filter
 * runs first, or the two configurations can silently disagree.
 */
@Configuration
class CorsConfig {

    private final List<String> allowedOrigins;

    CorsConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = Arrays.asList(allowedOrigins);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
