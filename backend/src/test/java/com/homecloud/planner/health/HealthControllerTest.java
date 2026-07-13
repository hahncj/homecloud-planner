package com.homecloud.planner.health;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-11T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private Clock clock;

    @Test
    void healthReturnsUpStatusAndTimestamp() throws Exception {
        Mockito.when(clock.instant()).thenReturn(FIXED_INSTANT);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", equalTo("UP")))
                .andExpect(jsonPath("$.timestamp", equalTo(FIXED_INSTANT.toString())));
    }

    @Test
    void unknownRouteReturnsRfc9457ProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
