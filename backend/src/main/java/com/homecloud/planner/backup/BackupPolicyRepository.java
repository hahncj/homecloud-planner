package com.homecloud.planner.backup;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BackupPolicyRepository extends JpaRepository<BackupPolicy, UUID> {

    List<BackupPolicy> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
