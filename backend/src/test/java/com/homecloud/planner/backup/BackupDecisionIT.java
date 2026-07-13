package com.homecloud.planner.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.TestcontainersConfiguration;
import java.time.LocalDate;
import java.util.HashMap;
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
class BackupDecisionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void backupMatrixFlagsMissingCoverageAndOverdueVerification() throws Exception {
        UUID projectId = createProject("Backup Matrix Project");

        Map<String, Object> noCoverage = backupPolicyRequest("Photos", null, null, false, false, null);
        UUID noCoverageId = createBackupPolicy(projectId, noCoverage);

        Map<String, Object> fullCoverage =
                backupPolicyRequest("Documents", "NAS volume2", "Backblaze B2", true, true, LocalDate.now().toString());
        UUID fullCoverageId = createBackupPolicy(projectId, fullCoverage);

        mockMvc.perform(get("/api/v1/backup-policies/{id}", noCoverageId))
                .andExpect(jsonPath("$.coverageState").value("NONE"))
                .andExpect(jsonPath("$.missingLocalBackup").value(true))
                .andExpect(jsonPath("$.missingOffsiteBackup").value(true))
                .andExpect(jsonPath("$.verificationOverdue").value(true));

        mockMvc.perform(get("/api/v1/backup-policies/{id}", fullCoverageId))
                .andExpect(jsonPath("$.coverageState").value("FULL"))
                .andExpect(jsonPath("$.missingLocalBackup").value(false))
                .andExpect(jsonPath("$.missingOffsiteBackup").value(false))
                .andExpect(jsonPath("$.verificationOverdue").value(false));
    }

    @Test
    void flagsMissingEncryptionForSensitiveOffsiteData() throws Exception {
        UUID projectId = createProject("Encryption Warning Project");

        Map<String, Object> sensitiveUnencrypted =
                backupPolicyRequest("Financial records", "NAS volume2", "Backblaze B2", false, true, null);
        UUID id = createBackupPolicy(projectId, sensitiveUnencrypted);

        mockMvc.perform(get("/api/v1/backup-policies/{id}", id))
                .andExpect(jsonPath("$.missingEncryptionForSensitiveOffsite").value(true));
    }

    @Test
    void filtersByCoverageStateAndVerificationOverdue() throws Exception {
        UUID projectId = createProject("Backup Filter Project");
        createBackupPolicy(projectId, backupPolicyRequest("Photos", null, null, false, false, null));
        createBackupPolicy(
                projectId,
                backupPolicyRequest("Documents", "NAS volume2", "Backblaze B2", true, false, LocalDate.now().toString()));

        mockMvc.perform(get("/api/v1/projects/{id}/backup-policies", projectId).param("coverageState", "NONE"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Photos backup"));

        mockMvc.perform(get("/api/v1/projects/{id}/backup-policies", projectId).param("verificationOverdue", "false"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Documents backup"));
    }

    @Test
    void projectDeletionIsBlockedWhileBackupPoliciesOrDecisionsExist() throws Exception {
        UUID projectWithPolicy = createProject("Backup Policy Deletion Project");
        createBackupPolicy(projectWithPolicy, backupPolicyRequest("Photos", null, null, false, false, null));
        mockMvc.perform(delete("/api/v1/projects/{id}", projectWithPolicy)).andExpect(status().isConflict());

        UUID projectWithDecision = createProject("Decision Deletion Project");
        createDecision(projectWithDecision, decisionRequest("Use PostgreSQL", "ACCEPTED", null, null));
        mockMvc.perform(delete("/api/v1/projects/{id}", projectWithDecision)).andExpect(status().isConflict());
    }

    @Test
    void decisionCrudLifecycleAndRelatedEntitiesWork() throws Exception {
        UUID projectId = createProject("Decision Project");
        UUID deviceId = createDevice(projectId, "NAS");
        UUID serviceId = createService(projectId, "Plex");

        UUID decisionId = createDecision(
                projectId, decisionRequest("Adopt ZFS for storage", "PROPOSED", List.of(deviceId), List.of(serviceId)));

        MvcResult getResult = mockMvc.perform(get("/api/v1/decisions/{id}", decisionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relatedDevices.length()").value(1))
                .andExpect(jsonPath("$.relatedDevices[0].name").value("NAS"))
                .andExpect(jsonPath("$.relatedServices.length()").value(1))
                .andExpect(jsonPath("$.relatedServices[0].name").value("Plex"))
                .andReturn();
        assertThat(getResult.getResponse().getContentAsString()).contains("PROPOSED");

        Map<String, Object> update = decisionRequest("Adopt ZFS for storage", "ACCEPTED", List.of(deviceId), List.of());

        mockMvc.perform(put("/api/v1/decisions/{id}", decisionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.relatedServices.length()").value(0));

        mockMvc.perform(delete("/api/v1/decisions/{id}", decisionId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/decisions/{id}", decisionId)).andExpect(status().isNotFound());
    }

    @Test
    void decisionStatusFilterWorks() throws Exception {
        UUID projectId = createProject("Decision Filter Project");
        createDecision(projectId, decisionRequest("Proposed decision", "PROPOSED", null, null));
        createDecision(projectId, decisionRequest("Accepted decision", "ACCEPTED", null, null));

        mockMvc.perform(get("/api/v1/projects/{id}/decisions", projectId).param("status", "ACCEPTED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Accepted decision"));
    }

    @Test
    void decisionRequiresTitleAndDecisionText() throws Exception {
        UUID projectId = createProject("Decision Validation Project");
        Map<String, Object> invalid = new HashMap<>();
        invalid.put("title", "");
        invalid.put("status", "PROPOSED");
        invalid.put("decision", "");

        mockMvc.perform(post("/api/v1/projects/{id}/decisions", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    private UUID createProject(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "status", "PLANNING"))))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createDevice(UUID projectId, String name) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("lifecycleStatus", "ACTIVE");
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
        request.put("status", "OPERATIONAL");
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

    private UUID createBackupPolicy(UUID projectId, Map<String, Object> request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/backup-policies", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createDecision(UUID projectId, Map<String, Object> request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/decisions", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private Map<String, Object> backupPolicyRequest(
            String dataCategory,
            String localLocation,
            String offsiteLocation,
            boolean encrypted,
            boolean sensitive,
            String lastVerifiedDate) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", dataCategory + " backup");
        request.put("dataCategory", dataCategory);
        request.put("primaryLocation", "NAS volume1");
        if (localLocation != null) request.put("localBackupLocation", localLocation);
        if (offsiteLocation != null) request.put("offsiteBackupLocation", offsiteLocation);
        request.put("encrypted", encrypted);
        request.put("containsSensitiveData", sensitive);
        request.put("frequency", "DAILY");
        if (lastVerifiedDate != null) request.put("lastVerifiedDate", lastVerifiedDate);
        return request;
    }

    private Map<String, Object> decisionRequest(
            String title, String status, List<UUID> relatedDeviceIds, List<UUID> relatedServiceIds) {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("status", status);
        request.put("decision", "We will " + title.toLowerCase());
        if (relatedDeviceIds != null) request.put("relatedDeviceIds", relatedDeviceIds);
        if (relatedServiceIds != null) request.put("relatedServiceIds", relatedServiceIds);
        return request;
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
