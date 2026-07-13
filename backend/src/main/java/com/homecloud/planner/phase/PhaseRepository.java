package com.homecloud.planner.phase;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhaseRepository extends JpaRepository<Phase, UUID> {

    List<Phase> findByProjectIdOrderBySequenceAsc(UUID projectId);

    long countByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
