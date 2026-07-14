package com.homecloud.planner.device;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findByProjectId(UUID projectId);

    boolean existsByProjectId(UUID projectId);
}
