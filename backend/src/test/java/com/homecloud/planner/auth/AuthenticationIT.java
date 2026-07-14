package com.homecloud.planner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end coverage of the real session-cookie + double-submit-cookie CSRF
 * flow described in ADR-0008 — deliberately does NOT import
 * {@link com.homecloud.planner.SecurityTestConfiguration}, since the whole
 * point here is to exercise the actual filter chain rather than bypass it.
 *
 * <p>Deliberately never uses {@code SecurityMockMvcRequestPostProcessors.csrf()}:
 * the first use in a test class permanently swaps the live {@code CsrfFilter}'s
 * token repository (via reflection) for a fresh {@code HttpSessionCsrfTokenRepository},
 * silently breaking the app's actual cookie-based repository for every other
 * test sharing the cached Spring context — including ones that never call
 * {@code .with(csrf())} themselves. Requests here that need a valid token get
 * one for real, via {@link #obtainXsrfCookie()}.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIT {

    private static final String USERNAME = "auth-it-admin";
    private static final String PASSWORD = "Sup3rSecret1!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureAdminAccountExists() {
        if (adminUserRepository.findByUsername(USERNAME).isEmpty()) {
            adminUserRepository.save(new AdminUser(USERNAME, passwordEncoder.encode(PASSWORD)));
        }
    }

    @Test
    void unauthenticatedRequestToProtectedResourceReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Authentication is required."));
    }

    @Test
    void sessionEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void publicEndpointResponseCarriesXsrfTokenCookie() throws Exception {
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk()).andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void loginWithoutCsrfTokenIsRejected() throws Exception {
        Map<String, Object> credentials = Map.of("username", USERNAME, "password", PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorizedProblemDetail() throws Exception {
        Cookie xsrfCookie = obtainXsrfCookie();
        Map<String, Object> credentials = Map.of("username", USERNAME, "password", "wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }

    @Test
    void loginEstablishesRealCookieSessionUsableForSubsequentRequests() throws Exception {
        Cookie xsrfCookie = obtainXsrfCookie();
        Map<String, Object> credentials = Map.of("username", USERNAME, "password", PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andReturn();

        // MockMvc tracks HTTP sessions as in-memory MockHttpSession objects
        // rather than emitting a real JSESSIONID Set-Cookie header, so
        // continuity across requests is threaded through .session(...)
        // rather than a cookie (unlike CSRF, which really is cookie-based).
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/v1/auth/session").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }

    @Test
    void logoutClearsSessionSoSubsequentRequestsAreUnauthenticated() throws Exception {
        Cookie xsrfCookie = obtainXsrfCookie();
        Map<String, Object> credentials = Map.of("username", USERNAME, "password", PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(session)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/session").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void onlyHealthActuatorEndpointIsExposed() throws Exception {
        // An authenticated request is used here because unauthenticated
        // requests are rejected by the security filter chain before
        // Spring MVC's handler mapping is ever consulted, which would mask
        // the thing this test actually verifies: that excluding an
        // actuator endpoint from management.endpoints.web.exposure.include
        // means it isn't mapped at all, not just that it requires auth.
        mockMvc.perform(get("/actuator/beans").with(user(USERNAME).roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private Cookie obtainXsrfCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/health")).andReturn();
        Cookie xsrfCookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrfCookie).isNotNull();
        return xsrfCookie;
    }
}
