package com.homecloud.planner.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.SecurityTestConfiguration;
import com.homecloud.planner.roadmap.ProgressSummary;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@Import(SecurityTestConfiguration.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createRejectsBlankNameWithProblemDetail() throws Exception {
        Map<String, Object> request = Map.of("name", "", "status", "PLANNING");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void createReturnsCreatedProjectOnValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(
                id, "Personal Hybrid Cloud", null, ProjectStatus.PLANNING, null, null, null,
                Instant.now(), Instant.now(), ProgressSummary.EMPTY);
        when(projectService.createProject(any())).thenReturn(response);

        Map<String, Object> request = Map.of("name", "Personal Hybrid Cloud", "status", "PLANNING");

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Personal Hybrid Cloud"));
    }

    @Test
    void listReturnsProjectsFromService() throws Exception {
        ProjectResponse response = new ProjectResponse(
                UUID.randomUUID(), "Project", null, ProjectStatus.PLANNING, null, null, null,
                Instant.now(), Instant.now(), ProgressSummary.EMPTY);
        when(projectService.listProjects()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
