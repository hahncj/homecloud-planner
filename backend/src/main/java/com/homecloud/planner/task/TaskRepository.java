package com.homecloud.planner.task;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByPhaseId(UUID phaseId);

    List<Task> findByPhaseProjectId(UUID projectId);

    long countByPhaseId(UUID phaseId);

    boolean existsByPhaseId(UUID phaseId);
}
