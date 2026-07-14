package com.homecloud.planner.shopping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseItemResponse(
        UUID id,
        UUID projectId,
        UUID phaseId,
        String category,
        String productName,
        String manufacturer,
        String model,
        String description,
        int quantity,
        BigDecimal estimatedUnitPrice,
        BigDecimal actualUnitPrice,
        BigDecimal estimatedTotal,
        BigDecimal actualTotal,
        String vendor,
        String purchaseUrl,
        PurchaseStatus status,
        LocalDate purchaseDate,
        LocalDate deliveryDate,
        LocalDate warrantyExpiration,
        String receiptReference,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public static PurchaseItemResponse of(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getId(),
                item.getProject().getId(),
                item.getPhase() == null ? null : item.getPhase().getId(),
                item.getCategory(),
                item.getProductName(),
                item.getManufacturer(),
                item.getModel(),
                item.getDescription(),
                item.getQuantity(),
                item.getEstimatedUnitPrice(),
                item.getActualUnitPrice(),
                item.getEstimatedTotal(),
                item.getActualTotal(),
                item.getVendor(),
                item.getPurchaseUrl(),
                item.getStatus(),
                item.getPurchaseDate(),
                item.getDeliveryDate(),
                item.getWarrantyExpiration(),
                item.getReceiptReference(),
                item.getNotes(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
