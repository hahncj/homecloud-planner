package com.homecloud.planner.seed;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.homecloud.planner.SecurityTestConfiguration;
import com.homecloud.planner.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Without the "dev" profile active (the default for this test suite, and
 * for any real deployment that doesn't explicitly opt in), the seed
 * controller bean does not exist at all — see ADR-0006.
 */
@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class SeedEndpointNotAvailableByDefaultIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void seedEndpointDoesNotExistWithoutTheDevProfile() throws Exception {
        mockMvc.perform(post("/api/v1/dev/seed")).andExpect(status().isNotFound());
    }
}
