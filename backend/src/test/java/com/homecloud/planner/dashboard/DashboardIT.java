package com.homecloud.planner.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.SecurityTestConfiguration;
import com.homecloud.planner.TestcontainersConfiguration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class DashboardIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardAggregatesProgressBudgetAndCounts() throws Exception {
        UUID projectId = createProject("Dashboard Project", "1000.00");
        UUID phaseId = createPhase(projectId, "Foundation");

        UUID doneTaskId = createTask(projectId, phaseId, "Rack switch", "NOT_STARTED");
        setTaskStatus(doneTaskId, "COMPLETED");
        UUID prereqId = createTask(projectId, phaseId, "Select NAS", "NOT_STARTED");
        UUID blockedTaskId = createTask(projectId, phaseId, "Configure NAS", "NOT_STARTED");
        addDependency(blockedTaskId, prereqId);

        createPurchaseItem(projectId, "Networking", "Switch", "100.00", "PLANNED");
        createDevice(projectId, "NAS", "ACTIVE");
        createService(projectId, "Plex");
        createBackupPolicy(projectId, "Photos", null);

        MvcResult result = mockMvc.perform(get("/api/v1/projects/{id}/dashboard", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Dashboard Project"))
                .andExpect(jsonPath("$.totalBudget").value(1000.00))
                .andExpect(jsonPath("$.overallProgress.taskCount").value(3))
                .andExpect(jsonPath("$.overallProgress.completedCount").value(1))
                .andExpect(jsonPath("$.blockedTaskCount").value(1))
                .andExpect(jsonPath("$.blockedTasks[0].title").value("Configure NAS"))
                .andExpect(jsonPath("$.currentPhase.name").value("Foundation"))
                .andExpect(jsonPath("$.purchaseStatusCounts.PLANNED").value(1))
                .andExpect(jsonPath("$.deviceLifecycleCounts.ACTIVE").value(1))
                .andExpect(jsonPath("$.serviceStatusCounts.PLANNED").value(1))
                .andExpect(jsonPath("$.backupCoverageWarnings.missingLocalBackupCount").value(1))
                .andExpect(jsonPath("$.backupCoverageWarnings.verificationOverdueCount").value(1))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("BLOCKING_TASK");
    }

    @Test
    void upcomingTargetDatesAndRecentCompletionsAreSurfaced() throws Exception {
        UUID projectId = createProject("Dates Project", null);
        UUID phaseId = createPhase(projectId, "Foundation");

        UUID upcomingTaskId = createTask(projectId, phaseId, "Set up UPS", "NOT_STARTED");
        setTaskTargetDate(upcomingTaskId, LocalDate.now().plusDays(5).toString());

        UUID completedTaskId = createTask(projectId, phaseId, "Inventory equipment", "NOT_STARTED");
        setTaskCompleted(completedTaskId, LocalDate.now().minusDays(2).toString());

        mockMvc.perform(get("/api/v1/projects/{id}/dashboard", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upcomingTargetDates[0].title").value("Set up UPS"))
                .andExpect(jsonPath("$.recentCompletedTasks[0].title").value("Inventory equipment"));
    }

    private UUID createProject(String name, String budget) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("status", "PLANNING");
        if (budget != null) request.put("budget", budget);
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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

    private UUID createTask(UUID projectId, UUID phaseId, String title, String status) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("status", status);
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

    private void setTaskStatus(UUID taskId, String status) throws Exception {
        updateTask(taskId, Map.of("status", status));
    }

    private void setTaskTargetDate(UUID taskId, String targetDate) throws Exception {
        updateTask(taskId, Map.of("status", "NOT_STARTED", "targetDate", targetDate));
    }

    private void setTaskCompleted(UUID taskId, String completedDate) throws Exception {
        updateTask(taskId, Map.of("status", "COMPLETED", "completedDate", completedDate));
    }

    private void updateTask(UUID taskId, Map<String, Object> overrides) throws Exception {
        MvcResult current = mockMvc.perform(get("/api/v1/tasks/{id}", taskId)).andReturn();
        Map<?, ?> body = objectMapper.readValue(current.getResponse().getContentAsString(), Map.class);
        Map<String, Object> request = new HashMap<>();
        request.put("title", body.get("title"));
        request.put("priority", body.get("priority"));
        request.put("status", body.get("status"));
        request.putAll(overrides);
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private UUID createPurchaseItem(UUID projectId, String category, String productName, String estimatedUnitPrice, String status)
            throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("category", category);
        request.put("productName", productName);
        request.put("quantity", 1);
        request.put("estimatedUnitPrice", estimatedUnitPrice);
        request.put("status", status);
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/purchase-items", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createDevice(UUID projectId, String name, String lifecycleStatus) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("lifecycleStatus", lifecycleStatus);
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createService(UUID projectId, String name) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("status", "PLANNED");
        request.put("runtimeType", "DOCKER");
        request.put("sensitivity", "INTERNAL");
        request.put("externallyExposed", false);
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/services", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createBackupPolicy(UUID projectId, String dataCategory, String lastVerifiedDate) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", dataCategory + " backup");
        request.put("dataCategory", dataCategory);
        request.put("primaryLocation", "NAS");
        request.put("encrypted", false);
        request.put("containsSensitiveData", false);
        request.put("frequency", "DAILY");
        if (lastVerifiedDate != null) request.put("lastVerifiedDate", lastVerifiedDate);
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/backup-policies", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
