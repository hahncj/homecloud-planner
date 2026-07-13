package com.homecloud.planner.dashboard;

import java.time.LocalDate;
import java.util.UUID;

public record WarrantyExpirationSummary(String entityType, UUID id, String name, LocalDate warrantyExpiration) {

    public static final String DEVICE = "DEVICE";
    public static final String PURCHASE_ITEM = "PURCHASE_ITEM";
}
