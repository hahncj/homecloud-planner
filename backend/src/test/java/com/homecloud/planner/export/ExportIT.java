package com.homecloud.planner.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.SecurityTestConfiguration;
import com.homecloud.planner.TestcontainersConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class ExportIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void jsonExportIncludesProjectPhasesTasksAndDependencies() throws Exception {
        UUID projectId = createProject("JSON Export Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID prereqId = createTask(projectId, phaseId, "Select NAS");
        UUID dependentId = createTask(projectId, phaseId, "Configure NAS");
        addDependency(dependentId, prereqId);

        mockMvc.perform(get("/api/v1/export/json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(jsonPath("$.exportedAt").exists())
                .andExpect(jsonPath("$.projects[?(@.project.id == '" + projectId + "')]").exists())
                .andExpect(jsonPath("$.projects[?(@.project.id == '" + projectId + "')].tasks.length()").value(2))
                .andExpect(jsonPath("$.projects[?(@.project.id == '" + projectId + "')].taskDependencies.length()").value(1));
    }

    @Test
    void markdownExportIsReadableAndContainsProjectContent() throws Exception {
        UUID projectId = createProject("Markdown Export Project");
        createPhase(projectId, "Foundation");

        MvcResult result = mockMvc.perform(get("/api/v1/export/markdown"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString("text/markdown")))
                .andReturn();

        String markdown = result.getResponse().getContentAsString();
        assertThat(markdown).contains("# HomeCloud Planner Export");
        assertThat(markdown).contains("Markdown Export Project");
        assertThat(markdown).contains("### Phases");
        assertThat(markdown).contains("Foundation");
        assertThat(markdown).doesNotContainIgnoringCase("password");
        assertThat(markdown).doesNotContainIgnoringCase("credential");
    }

    private UUID createProject(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "status", "PLANNING"))))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createPhase(UUID projectId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/phases", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createTask(UUID projectId, UUID phaseId, String title) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("status", "NOT_STARTED");
        request.put("priority", "MEDIUM");
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{projectId}/phases/{phaseId}/tasks", projectId, phaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private void addDependency(UUID taskId, UUID dependsOnTaskId) throws Exception {
        mockMvc.perform(post("/api/v1/tasks/{id}/dependencies", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dependsOnTaskId", dependsOnTaskId))))
                .andExpect(status().isCreated());
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
