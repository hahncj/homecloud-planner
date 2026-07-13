package com.homecloud.planner.shopping;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;

public record PurchaseItemRequest(
        UUID phaseId,
        @NotBlank @Size(max = 100) String category,
        @NotBlank @Size(max = 200) String productName,
        String manufacturer,
        String model,
        String description,
        @NotNull @Min(1) Integer quantity,
        @DecimalMin(value = "0", message = "estimatedUnitPrice must not be negative") BigDecimal estimatedUnitPrice,
        @DecimalMin(value = "0", message = "actualUnitPrice must not be negative") BigDecimal actualUnitPrice,
        String vendor,
        @URL(message = "purchaseUrl must be a valid URL") String purchaseUrl,
        @NotNull PurchaseStatus status,
        LocalDate purchaseDate,
        LocalDate deliveryDate,
        LocalDate warrantyExpiration,
        String receiptReference,
        String notes) {
}
