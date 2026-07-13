package com.homecloud.planner.servicecatalog;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceDependencyRepository extends JpaRepository<ServiceDependency, UUID> {

    List<ServiceDependency> findByServiceId(UUID serviceId);

    List<ServiceDependency> findByServiceProjectId(UUID projectId);

    boolean existsByServiceIdAndDependsOnServiceId(UUID serviceId, UUID dependsOnServiceId);

    boolean existsByDependsOnServiceId(UUID dependsOnServiceId);

    void deleteByServiceIdAndDependsOnServiceId(UUID serviceId, UUID dependsOnServiceId);
}
