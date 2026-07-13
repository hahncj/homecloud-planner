package com.homecloud.planner.shopping;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    List<PurchaseItem> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
