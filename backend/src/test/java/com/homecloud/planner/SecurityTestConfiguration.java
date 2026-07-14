package com.homecloud.planner;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Domain-behavior tests (roadmap, shopping, hardware, backup, dashboard,
 * export, ...) are about business rules, not authentication — Milestone 7
 * added authentication as a cross-cutting concern to every endpoint, so
 * rather than adding {@code .with(csrf())}/{@code .with(user(...))} to
 * every existing request in every existing test, this applies both as
 * defaults to every request the shared {@code MockMvc} builds. Tests that
 * specifically exercise login/logout/authorization import
 * {@code TestcontainersConfiguration} only, not this class, so they see
 * real (unauthenticated) behavior.
 */
@TestConfiguration(proxyBeanMethods = false)
public class SecurityTestConfiguration {

    @Bean
    public MockMvcBuilderCustomizer authenticatedCsrfMockMvcCustomizer() {
        return builder -> builder.defaultRequest(MockMvcRequestBuilders.get("/")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .with(SecurityMockMvcRequestPostProcessors.user("test-admin").roles("ADMIN")));
    }
}
