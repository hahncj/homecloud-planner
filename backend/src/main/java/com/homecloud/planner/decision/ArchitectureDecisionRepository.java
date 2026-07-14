package com.homecloud.planner.decision;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchitectureDecisionRepository extends JpaRepository<ArchitectureDecision, UUID> {

    List<ArchitectureDecision> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
