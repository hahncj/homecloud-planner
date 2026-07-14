package com.homecloud.planner.servicecatalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedServiceRepository extends JpaRepository<ManagedService, UUID> {

    List<ManagedService> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
