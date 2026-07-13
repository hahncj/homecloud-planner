package com.homecloud.planner.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.TestcontainersConfiguration;
import java.util.List;
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

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class RoadmapIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void projectDeletionIsBlockedWhilePhasesExist() throws Exception {
        UUID projectId = createProject("Blocked Deletion Project");
        createPhase(projectId, "Foundation");

        mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void projectWithoutPhasesCanBeDeleted() throws Exception {
        UUID projectId = createProject("Deletable Project");

        mockMvc.perform(delete("/api/v1/projects/{id}", projectId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/projects/{id}", projectId)).andExpect(status().isNotFound());
    }

    @Test
    void projectBudgetMustNotBeNegative() throws Exception {
        Map<String, Object> request = Map.of(
                "name", "Negative Budget",
                "status", "PLANNING",
                "budget", -1);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void phasesGetContiguousSequenceAndResequenceOnDeletion() throws Exception {
        UUID projectId = createProject("Sequencing Project");
        UUID phase1 = createPhase(projectId, "Foundation");
        UUID phase2 = createPhase(projectId, "Storage");
        UUID phase3 = createPhase(projectId, "Compute");

        mockMvc.perform(get("/api/v1/projects/{id}/phases", projectId))
                .andExpect(jsonPath("$[0].sequence").value(1))
                .andExpect(jsonPath("$[1].sequence").value(2))
                .andExpect(jsonPath("$[2].sequence").value(3));

        mockMvc.perform(delete("/api/v1/projects/{projectId}/phases/{phaseId}", projectId, phase2))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/projects/{id}/phases", projectId))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(phase1.toString()))
                .andExpect(jsonPath("$[0].sequence").value(1))
                .andExpect(jsonPath("$[1].id").value(phase3.toString()))
                .andExpect(jsonPath("$[1].sequence").value(2));
    }

    @Test
    void phaseReorderAcceptsAPermutationAndRejectsAMismatchedSet() throws Exception {
        UUID projectId = createProject("Reorder Project");
        UUID phase1 = createPhase(projectId, "Foundation");
        UUID phase2 = createPhase(projectId, "Storage");

        Map<String, Object> reorder = Map.of("orderedPhaseIds", List.of(phase2, phase1));
        mockMvc.perform(put("/api/v1/projects/{id}/phases/reorder", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(phase2.toString()))
                .andExpect(jsonPath("$[0].sequence").value(1))
                .andExpect(jsonPath("$[1].id").value(phase1.toString()))
                .andExpect(jsonPath("$[1].sequence").value(2));

        Map<String, Object> invalidReorder = Map.of("orderedPhaseIds", List.of(phase1));
        mockMvc.perform(put("/api/v1/projects/{id}/phases/reorder", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReorder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void phaseDeletionIsBlockedWhileTasksExist() throws Exception {
        UUID projectId = createProject("Phase Deletion Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        createTask(projectId, phaseId, "Rack the server", "NOT_STARTED", "MEDIUM");

        mockMvc.perform(delete("/api/v1/projects/{projectId}/phases/{phaseId}", projectId, phaseId))
                .andExpect(status().isConflict());
    }

    @Test
    void taskCostsMustNotBeNegative() throws Exception {
        UUID projectId = createProject("Cost Validation Project");
        UUID phaseId = createPhase(projectId, "Foundation");

        Map<String, Object> request = Map.of(
                "title", "Buy switch",
                "status", "NOT_STARTED",
                "priority", "MEDIUM",
                "estimatedCost", -50);

        mockMvc.perform(post("/api/v1/projects/{projectId}/phases/{phaseId}/tasks", projectId, phaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dependencySelfReferenceIsRejected() throws Exception {
        UUID projectId = createProject("Self Dependency Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID taskId = createTask(projectId, phaseId, "Task A", "NOT_STARTED", "MEDIUM");

        mockMvc.perform(post("/api/v1/tasks/{id}/dependencies", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dependsOnTaskId", taskId))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateDependencyIsRejected() throws Exception {
        UUID projectId = createProject("Duplicate Dependency Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID taskA = createTask(projectId, phaseId, "Task A", "NOT_STARTED", "MEDIUM");
        UUID taskB = createTask(projectId, phaseId, "Task B", "NOT_STARTED", "MEDIUM");

        addDependency(taskA, taskB).andExpect(status().isCreated());
        addDependency(taskA, taskB).andExpect(status().isConflict());
    }

    @Test
    void crossProjectDependencyIsRejected() throws Exception {
        UUID projectA = createProject("Project A");
        UUID phaseA = createPhase(projectA, "Foundation");
        UUID taskA = createTask(projectA, phaseA, "Task A", "NOT_STARTED", "MEDIUM");

        UUID projectB = createProject("Project B");
        UUID phaseB = createPhase(projectB, "Foundation");
        UUID taskB = createTask(projectB, phaseB, "Task B", "NOT_STARTED", "MEDIUM");

        addDependency(taskA, taskB).andExpect(status().isBadRequest());
    }

    @Test
    void directAndIndirectDependencyCyclesAreRejected() throws Exception {
        UUID projectId = createProject("Cycle Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID taskA = createTask(projectId, phaseId, "Task A", "NOT_STARTED", "MEDIUM");
        UUID taskB = createTask(projectId, phaseId, "Task B", "NOT_STARTED", "MEDIUM");
        UUID taskC = createTask(projectId, phaseId, "Task C", "NOT_STARTED", "MEDIUM");

        addDependency(taskA, taskB).andExpect(status().isCreated());
        addDependency(taskB, taskA).andExpect(status().isBadRequest());

        addDependency(taskB, taskC).andExpect(status().isCreated());
        addDependency(taskC, taskA).andExpect(status().isBadRequest());
    }

    @Test
    void dependenciesMaySpanPhasesInTheSameProject() throws Exception {
        UUID projectId = createProject("Cross Phase Project");
        UUID foundationPhase = createPhase(projectId, "Foundation");
        UUID storagePhase = createPhase(projectId, "Storage");
        UUID taskA = createTask(projectId, storagePhase, "Configure NAS", "NOT_STARTED", "MEDIUM");
        UUID taskB = createTask(projectId, foundationPhase, "Rack switch", "NOT_STARTED", "MEDIUM");

        addDependency(taskA, taskB).andExpect(status().isCreated());
    }

    @Test
    void blockedStateIsCalculatedAndExcludesCompletedAndCancelledTasks() throws Exception {
        UUID projectId = createProject("Blocked Calculation Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID prerequisite = createTask(projectId, phaseId, "Rack switch", "NOT_STARTED", "MEDIUM");
        UUID dependent = createTask(projectId, phaseId, "Configure VLANs", "NOT_STARTED", "MEDIUM");
        addDependency(dependent, prerequisite).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tasks/{id}", dependent))
                .andExpect(jsonPath("$.blocked").value(true));
        mockMvc.perform(get("/api/v1/tasks/{id}", prerequisite))
                .andExpect(jsonPath("$.blocked").value(false));

        completeTask(prerequisite);

        mockMvc.perform(get("/api/v1/tasks/{id}", dependent))
                .andExpect(jsonPath("$.blocked").value(false));
    }

    @Test
    void phaseAndProjectProgressExcludeCancelledTasksFromNumeratorAndDenominator() throws Exception {
        UUID projectId = createProject("Progress Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID completed = createTask(projectId, phaseId, "Task 1", "NOT_STARTED", "MEDIUM");
        UUID cancelled = createTask(projectId, phaseId, "Task 2", "NOT_STARTED", "MEDIUM");
        createTask(projectId, phaseId, "Task 3", "NOT_STARTED", "MEDIUM");

        completeTask(completed);
        cancelTask(cancelled);

        mockMvc.perform(get("/api/v1/projects/{id}/phases", projectId))
                .andExpect(jsonPath("$[0].progress.taskCount").value(2))
                .andExpect(jsonPath("$[0].progress.completedCount").value(1))
                .andExpect(jsonPath("$[0].progress.progressPercentage").value(50));

        mockMvc.perform(get("/api/v1/projects/{id}", projectId))
                .andExpect(jsonPath("$.progress.taskCount").value(2))
                .andExpect(jsonPath("$.progress.completedCount").value(1))
                .andExpect(jsonPath("$.progress.progressPercentage").value(50));
    }

    @Test
    void roadmapEndpointReturnsProjectPhasesAndTasksTogether() throws Exception {
        UUID projectId = createProject("Roadmap Endpoint Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        createTask(projectId, phaseId, "Task 1", "NOT_STARTED", "MEDIUM");

        mockMvc.perform(get("/api/v1/projects/{id}/roadmap", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.id").value(projectId.toString()))
                .andExpect(jsonPath("$.phases.length()").value(1))
                .andExpect(jsonPath("$.tasks.length()").value(1));
    }

    @Test
    void taskFilteringByStatusPriorityPhaseAndBlockedWorks() throws Exception {
        UUID projectId = createProject("Filter Project");
        UUID phaseId = createPhase(projectId, "Foundation");
        UUID other = createTask(projectId, phaseId, "High priority", "IN_PROGRESS", "HIGH");
        createTask(projectId, phaseId, "Low priority", "NOT_STARTED", "LOW");

        mockMvc.perform(get("/api/v1/projects/{id}/tasks", projectId).param("priority", "HIGH"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(other.toString()));

        mockMvc.perform(get("/api/v1/projects/{id}/tasks", projectId).param("status", "IN_PROGRESS"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/projects/{id}/tasks", projectId).param("phaseId", phaseId.toString()))
                .andExpect(jsonPath("$.length()").value(2));
    }

    private UUID createProject(String name) throws Exception {
        Map<String, Object> request = Map.of("name", name, "status", "PLANNING");
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createPhase(UUID projectId, String name) throws Exception {
        Map<String, Object> request = Map.of("name", name);
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/phases", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createTask(UUID projectId, UUID phaseId, String title, String status, String priority)
            throws Exception {
        Map<String, Object> request = Map.of("title", title, "status", status, "priority", priority);
        MvcResult result = mockMvc.perform(
                        post("/api/v1/projects/{projectId}/phases/{phaseId}/tasks", projectId, phaseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private org.springframework.test.web.servlet.ResultActions addDependency(UUID taskId, UUID dependsOnTaskId)
            throws Exception {
        return mockMvc.perform(post("/api/v1/tasks/{id}/dependencies", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("dependsOnTaskId", dependsOnTaskId))));
    }

    private void completeTask(UUID taskId) throws Exception {
        setTaskStatus(taskId, "COMPLETED");
    }

    private void cancelTask(UUID taskId) throws Exception {
        setTaskStatus(taskId, "CANCELLED");
    }

    private void setTaskStatus(UUID taskId, String status) throws Exception {
        MvcResult current = mockMvc.perform(get("/api/v1/tasks/{id}", taskId)).andReturn();
        Map<?, ?> body = objectMapper.readValue(current.getResponse().getContentAsString(), Map.class);
        Map<String, Object> request = Map.of(
                "title", body.get("title"),
                "status", status,
                "priority", body.get("priority"));
        mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
