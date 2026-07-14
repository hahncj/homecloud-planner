package com.homecloud.planner.task;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    List<TaskDependency> findByTaskId(UUID taskId);

    List<TaskDependency> findByTaskIdIn(Collection<UUID> taskIds);

    List<TaskDependency> findByTaskPhaseProjectId(UUID projectId);

    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);

    void deleteByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
}
