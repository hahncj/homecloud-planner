package com.homecloud.planner.shopping;

import java.util.UUID;

public record PurchaseItemFilter(PurchaseStatus status, String category, UUID phaseId) {

    public static final PurchaseItemFilter NONE = new PurchaseItemFilter(null, null, null);
}
