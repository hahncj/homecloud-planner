package com.homecloud.planner.servicecatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homecloud.planner.TestcontainersConfiguration;
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
import org.springframework.test.web.servlet.ResultActions;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class HardwareServiceCatalogIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deviceRejectsInvalidIpAndMacAddresses() throws Exception {
        UUID projectId = createProject("IP Validation Project");

        Map<String, Object> badIp = deviceRequest("Switch", "ACTIVE");
        badIp.put("ipAddress", "999.999.999.999");
        mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badIp)))
                .andExpect(status().isBadRequest());

        Map<String, Object> badMac = deviceRequest("Switch", "ACTIVE");
        badMac.put("macAddress", "not-a-mac");
        mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badMac)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deviceAcceptsValidNetworkFieldsAndLeavesThemOptional() throws Exception {
        UUID projectId = createProject("Network Fields Project");

        Map<String, Object> withNetwork = deviceRequest("Switch", "ACTIVE");
        withNetwork.put("ipAddress", "192.168.1.10");
        withNetwork.put("macAddress", "AA:BB:CC:DD:EE:FF");
        withNetwork.put("vlan", 10);
        mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withNetwork)))
                .andExpect(status().isCreated());

        Map<String, Object> withoutNetwork = deviceRequest("Spare NUC", "SPARE");
        mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withoutNetwork)))
                .andExpect(status().isCreated());
    }

    @Test
    void deviceLifecycleFilterNarrowsTheList() throws Exception {
        UUID projectId = createProject("Lifecycle Filter Project");
        createDevice(projectId, deviceRequest("Router", "ACTIVE"));
        createDevice(projectId, deviceRequest("Old Switch", "RETIRED"));

        mockMvc.perform(get("/api/v1/projects/{id}/devices", projectId).param("lifecycleStatus", "RETIRED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Old Switch"));
    }

    @Test
    void deletingHostDeviceClearsItFromServiceRatherThanBlockingOrCascading() throws Exception {
        UUID projectId = createProject("Host Device Deletion Project");
        UUID deviceId = createDevice(projectId, deviceRequest("NAS", "ACTIVE"));
        UUID serviceId = createService(projectId, serviceRequest("Plex", deviceId));

        mockMvc.perform(delete("/api/v1/devices/{id}", deviceId)).andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/api/v1/services/{id}", serviceId))
                .andExpect(status().isOk())
                .andReturn();
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(body.get("hostDeviceId")).isNull();
    }

    @Test
    void projectDeletionIsBlockedWhileDevicesOrServicesExist() throws Exception {
        UUID projectWithDevice = createProject("Device Deletion Project");
        createDevice(projectWithDevice, deviceRequest("Router", "ACTIVE"));
        mockMvc.perform(delete("/api/v1/projects/{id}", projectWithDevice)).andExpect(status().isConflict());

        UUID projectWithService = createProject("Service Deletion Project");
        createService(projectWithService, serviceRequest("Plex", null));
        mockMvc.perform(delete("/api/v1/projects/{id}", projectWithService)).andExpect(status().isConflict());
    }

    @Test
    void serviceDependencySelfDuplicateAndCycleAreRejected() throws Exception {
        UUID projectId = createProject("Service Dependency Project");
        UUID serviceA = createService(projectId, serviceRequest("Reverse Proxy", null));
        UUID serviceB = createService(projectId, serviceRequest("Auth Provider", null));
        UUID serviceC = createService(projectId, serviceRequest("Database", null));

        addDependency(serviceA, serviceA).andExpect(status().isBadRequest());

        addDependency(serviceA, serviceB).andExpect(status().isCreated());
        addDependency(serviceA, serviceB).andExpect(status().isConflict());

        addDependency(serviceB, serviceC).andExpect(status().isCreated());
        addDependency(serviceC, serviceA).andExpect(status().isBadRequest());
    }

    @Test
    void serviceDeletionIsBlockedWhileOtherServicesDependOnIt() throws Exception {
        UUID projectId = createProject("Service Deletion Blocked Project");
        UUID reverseProxy = createService(projectId, serviceRequest("Reverse Proxy", null));
        UUID authProvider = createService(projectId, serviceRequest("Auth Provider", null));
        addDependency(reverseProxy, authProvider).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/services/{id}", authProvider)).andExpect(status().isConflict());

        // The dependent service can still be deleted; it has no dependents of its own.
        mockMvc.perform(delete("/api/v1/services/{id}", reverseProxy)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/services/{id}", authProvider)).andExpect(status().isNoContent());
    }

    @Test
    void serviceFiltersByStatusRuntimeTypeSensitivityAndExposure() throws Exception {
        UUID projectId = createProject("Service Filter Project");
        Map<String, Object> exposed = serviceRequest("Public Wiki", null);
        exposed.put("externallyExposed", true);
        exposed.put("sensitivity", "PUBLIC");
        createService(projectId, exposed);

        Map<String, Object> internal = serviceRequest("Internal Dashboard", null);
        internal.put("externallyExposed", false);
        internal.put("sensitivity", "CONFIDENTIAL");
        createService(projectId, internal);

        mockMvc.perform(get("/api/v1/projects/{id}/services", projectId).param("externallyExposed", "true"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Public Wiki"));

        mockMvc.perform(get("/api/v1/projects/{id}/services", projectId).param("sensitivity", "CONFIDENTIAL"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Internal Dashboard"));
    }

    private UUID createProject(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name, "status", "PLANNING"))))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createDevice(UUID projectId, Map<String, Object> request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/devices", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private UUID createService(UUID projectId, Map<String, Object> request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/services", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private ResultActions addDependency(UUID serviceId, UUID dependsOnServiceId) throws Exception {
        return mockMvc.perform(post("/api/v1/services/{id}/dependencies", serviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("dependsOnServiceId", dependsOnServiceId))));
    }

    private Map<String, Object> deviceRequest(String name, String lifecycleStatus) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("lifecycleStatus", lifecycleStatus);
        return request;
    }

    private Map<String, Object> serviceRequest(String name, UUID hostDeviceId) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("status", "OPERATIONAL");
        request.put("runtimeType", "DOCKER");
        request.put("sensitivity", "INTERNAL");
        request.put("externallyExposed", false);
        if (hostDeviceId != null) {
            request.put("hostDeviceId", hostDeviceId.toString());
        }
        return request;
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
