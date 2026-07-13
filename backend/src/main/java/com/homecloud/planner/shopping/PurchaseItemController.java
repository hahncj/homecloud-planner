package com.homecloud.planner.shopping;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class PurchaseItemController {

    private final PurchaseItemService purchaseItemService;

    PurchaseItemController(PurchaseItemService purchaseItemService) {
        this.purchaseItemService = purchaseItemService;
    }

    @GetMapping("/projects/{projectId}/purchase-items")
    List<PurchaseItemResponse> listPurchaseItems(
            @PathVariable UUID projectId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID phaseId) {
        return purchaseItemService.listPurchaseItems(projectId, new PurchaseItemFilter(status, category, phaseId));
    }

    @GetMapping("/purchase-items/{purchaseItemId}")
    PurchaseItemResponse getPurchaseItem(@PathVariable UUID purchaseItemId) {
        return purchaseItemService.getPurchaseItem(purchaseItemId);
    }

    @PostMapping("/projects/{projectId}/purchase-items")
    ResponseEntity<PurchaseItemResponse> createPurchaseItem(
            @PathVariable UUID projectId, @Valid @RequestBody PurchaseItemRequest request) {
        PurchaseItemResponse created = purchaseItemService.createPurchaseItem(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/purchase-items/{purchaseItemId}")
    PurchaseItemResponse updatePurchaseItem(
            @PathVariable UUID purchaseItemId, @Valid @RequestBody PurchaseItemRequest request) {
        return purchaseItemService.updatePurchaseItem(purchaseItemId, request);
    }

    @DeleteMapping("/purchase-items/{purchaseItemId}")
    ResponseEntity<Void> deletePurchaseItem(@PathVariable UUID purchaseItemId) {
        purchaseItemService.deletePurchaseItem(purchaseItemId);
        return ResponseEntity.noContent().build();
    }
}
