package com.homecloud.planner.shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import({TestcontainersConfiguration.class, SecurityTestConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class ShoppingBudgetIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void quantityMustBePositive() throws Exception {
        UUID projectId = createProject("Quantity Validation Project", null);

        Map<String, Object> request = purchaseRequest("Networking", "Switch", 0, "50.00", null, "PLANNED");
        mockMvc.perform(post("/api/v1/projects/{id}/purchase-items", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unitPricesMustNotBeNegative() throws Exception {
        UUID projectId = createProject("Price Validation Project", null);

        Map<String, Object> request = purchaseRequest("Networking", "Switch", 1, "-5.00", null, "PLANNED");
        mockMvc.perform(post("/api/v1/projects/{id}/purchase-items", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void totalsAreCalculatedFromUnitPriceAndQuantity() throws Exception {
        UUID projectId = createProject("Totals Project", null);

        Map<String, Object> request =
                purchaseRequest("Networking", "Switch", 3, "20.00", "18.50", "RECEIVED");
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/purchase-items", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        assertThat(new java.math.BigDecimal(body.get("estimatedTotal").toString())).isEqualByComparingTo("60.00");
        assertThat(new java.math.BigDecimal(body.get("actualTotal").toString())).isEqualByComparingTo("55.50");
    }

    @Test
    void statusCategoryAndPhaseFiltersNarrowTheList() throws Exception {
        UUID projectId = createProject("Filter Project", null);
        UUID phaseId = createPhase(projectId, "Foundation");

        createPurchaseItem(projectId, purchaseRequestWithPhase("Networking", "Switch", 1, "10.00", null, "PLANNED", phaseId));
        createPurchaseItem(projectId, purchaseRequest("Storage", "Drive", 2, "80.00", null, "IDEA"));

        mockMvc.perform(get("/api/v1/projects/{id}/purchase-items", projectId).param("category", "Storage"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Drive"));

        mockMvc.perform(get("/api/v1/projects/{id}/purchase-items", projectId).param("status", "PLANNED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Switch"));

        mockMvc.perform(get("/api/v1/projects/{id}/purchase-items", projectId).param("phaseId", phaseId.toString()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Switch"));
    }

    @Test
    void budgetSummaryExcludesCancelledItemsAndPrefersActualCost() throws Exception {
        UUID projectId = createProject("Budget Summary Project", "1000.00");

        createPurchaseItem(projectId, purchaseRequest("Networking", "Switch", 1, "100.00", "90.00", "RECEIVED"));
        createPurchaseItem(projectId, purchaseRequest("Networking", "Cable", 5, "5.00", null, "PLANNED"));
        createPurchaseItem(projectId, purchaseRequest("Storage", "Drive", 1, "500.00", null, "CANCELLED"));

        mockMvc.perform(get("/api/v1/projects/{id}/budget", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budget").value(1000.00))
                // committed = 90.00 (actual wins) + 25.00 (estimate, no actual yet) = 115.00; cancelled excluded.
                .andExpect(jsonPath("$.committedSpending").value(115.00))
                .andExpect(jsonPath("$.remainingBudget").value(885.00))
                .andExpect(jsonPath("$.categories.length()").value(1))
                .andExpect(jsonPath("$.categories[0].category").value("Networking"));
    }

    @Test
    void projectDeletionIsBlockedWhilePurchaseItemsExist() throws Exception {
        UUID projectId = createProject("Purchase Deletion Project", null);
        createPurchaseItem(projectId, purchaseRequest("Networking", "Switch", 1, "10.00", null, "IDEA"));

        mockMvc.perform(delete("/api/v1/projects/{id}", projectId))
                .andExpect(status().isConflict());
    }

    @Test
    void purchaseItemCrudLifecycleWorks() throws Exception {
        UUID projectId = createProject("CRUD Project", null);
        UUID itemId = createPurchaseItem(projectId, purchaseRequest("Networking", "Switch", 1, "10.00", null, "IDEA"));

        mockMvc.perform(get("/api/v1/purchase-items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Switch"));

        Map<String, Object> update = purchaseRequest("Networking", "Managed Switch", 1, "15.00", null, "ORDERED");
        mockMvc.perform(put("/api/v1/purchase-items/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Managed Switch"))
                .andExpect(jsonPath("$.status").value("ORDERED"));

        mockMvc.perform(delete("/api/v1/purchase-items/{id}", itemId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/purchase-items/{id}", itemId)).andExpect(status().isNotFound());
    }

    private UUID createProject(String name, String budget) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("status", "PLANNING");
        if (budget != null) {
            request.put("budget", budget);
        }
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

    private UUID createPurchaseItem(UUID projectId, Map<String, Object> request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/projects/{id}/purchase-items", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(readField(result, "id"));
    }

    private Map<String, Object> purchaseRequest(
            String category, String productName, int quantity, String estimatedUnitPrice, String actualUnitPrice, String status) {
        Map<String, Object> request = new HashMap<>();
        request.put("category", category);
        request.put("productName", productName);
        request.put("quantity", quantity);
        if (estimatedUnitPrice != null) request.put("estimatedUnitPrice", estimatedUnitPrice);
        if (actualUnitPrice != null) request.put("actualUnitPrice", actualUnitPrice);
        request.put("status", status);
        return request;
    }

    private Map<String, Object> purchaseRequestWithPhase(
            String category,
            String productName,
            int quantity,
            String estimatedUnitPrice,
            String actualUnitPrice,
            String status,
            UUID phaseId) {
        Map<String, Object> request = purchaseRequest(category, productName, quantity, estimatedUnitPrice, actualUnitPrice, status);
        request.put("phaseId", phaseId.toString());
        return request;
    }

    private String readField(MvcResult result, String field) throws Exception {
        Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        Object value = body.get(field);
        assertThat(value).isNotNull();
        return value.toString();
    }
}
