package com.homecloud.planner.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the frontend's origin to call the API from a browser. The frontend and
 * backend are served from different origins in every supported setup (Vite dev
 * server on :3000 calling Spring Boot on :8080, or the nginx container calling
 * the backend container), so without this, all browser requests are blocked by
 * the same-origin policy.
 */
@Configuration
class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    CorsConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
