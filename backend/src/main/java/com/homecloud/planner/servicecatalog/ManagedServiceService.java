package com.homecloud.planner.servicecatalog;

import com.homecloud.planner.common.ConflictException;
import com.homecloud.planner.common.DependencyGraphs;
import com.homecloud.planner.common.InvalidRequestException;
import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.device.DeviceRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManagedServiceService {

    private final ManagedServiceRepository serviceRepository;
    private final ServiceDependencyRepository dependencyRepository;
    private final ProjectRepository projectRepository;
    private final DeviceRepository deviceRepository;

    public ManagedServiceService(
            ManagedServiceRepository serviceRepository,
            ServiceDependencyRepository dependencyRepository,
            ProjectRepository projectRepository,
            DeviceRepository deviceRepository) {
        this.serviceRepository = serviceRepository;
        this.dependencyRepository = dependencyRepository;
        this.projectRepository = projectRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ManagedServiceResponse> listServices(UUID projectId, ManagedServiceFilter filter) {
        requireProject(projectId);
        List<ManagedService> services = serviceRepository.findByProjectId(projectId);
        Map<UUID, List<UUID>> dependsOnByService = groupDependsOn(dependencyRepository.findByServiceProjectId(projectId));
        return services.stream()
                .filter(service -> matches(service, filter))
                .map(service -> ManagedServiceResponse.of(service, dependsOnByService.getOrDefault(service.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ManagedServiceResponse getService(UUID serviceId) {
        ManagedService service = requireService(serviceId);
        List<UUID> dependsOnServiceIds = dependencyRepository.findByServiceId(serviceId).stream()
                .map(dependency -> dependency.getDependsOnService().getId())
                .toList();
        return ManagedServiceResponse.of(service, dependsOnServiceIds);
    }

    @Transactional
    public ManagedServiceResponse createService(UUID projectId, ManagedServiceRequest request) {
        Project project = requireProject(projectId);
        Device hostDevice = requireDeviceInProjectIfPresent(projectId, request.hostDeviceId());
        ManagedService service = new ManagedService(
                project,
                hostDevice,
                request.name(),
                request.purpose(),
                request.description(),
                request.status(),
                request.runtimeType(),
                request.storageLocation(),
                request.sensitivity(),
                request.externallyExposed(),
                request.authenticationMethod(),
                request.backupPolicy(),
                request.documentationUrl(),
                request.repositoryUrl(),
                request.notes());
        ManagedService saved = serviceRepository.save(service);
        return ManagedServiceResponse.of(saved, List.of());
    }

    @Transactional
    public ManagedServiceResponse updateService(UUID serviceId, ManagedServiceRequest request) {
        ManagedService service = requireService(serviceId);
        service.setHostDevice(requireDeviceInProjectIfPresent(service.getProject().getId(), request.hostDeviceId()));
        service.setName(request.name());
        service.setPurpose(request.purpose());
        service.setDescription(request.description());
        service.setStatus(request.status());
        service.setRuntimeType(request.runtimeType());
        service.setStorageLocation(request.storageLocation());
        service.setSensitivity(request.sensitivity());
        service.setExternallyExposed(request.externallyExposed());
        service.setAuthenticationMethod(request.authenticationMethod());
        service.setBackupPolicy(request.backupPolicy());
        service.setDocumentationUrl(request.documentationUrl());
        service.setRepositoryUrl(request.repositoryUrl());
        service.setNotes(request.notes());
        return getService(serviceId);
    }

    /**
     * Deletion must not silently remove relationships: if another service
     * still depends on this one, the caller has to remove that dependency
     * first rather than have it disappear as a side effect of this delete.
     */
    @Transactional
    public void deleteService(UUID serviceId) {
        ManagedService service = requireService(serviceId);
        if (dependencyRepository.existsByDependsOnServiceId(serviceId)) {
            throw new ConflictException(
                    "Other services depend on this service; remove those dependencies before deleting it.");
        }
        serviceRepository.delete(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceDependencyResponse> listDependencies(UUID serviceId) {
        requireService(serviceId);
        return dependencyRepository.findByServiceId(serviceId).stream()
                .map(ServiceDependencyResponse::of)
                .toList();
    }

    @Transactional
    public ServiceDependencyResponse addDependency(UUID serviceId, ServiceDependencyRequest request) {
        UUID dependsOnServiceId = request.dependsOnServiceId();
        if (serviceId.equals(dependsOnServiceId)) {
            throw new InvalidRequestException("A service cannot depend on itself.");
        }

        ManagedService service = requireService(serviceId);
        ManagedService dependsOnService = requireService(dependsOnServiceId);

        if (dependencyRepository.existsByServiceIdAndDependsOnServiceId(serviceId, dependsOnServiceId)) {
            throw new ConflictException("This dependency already exists.");
        }

        List<DependencyGraphs.Edge> existingEdges = dependencyRepository.findByServiceProjectId(service.getProject().getId())
                .stream()
                .map(dependency -> new DependencyGraphs.Edge(
                        dependency.getService().getId(), dependency.getDependsOnService().getId()))
                .toList();
        if (DependencyGraphs.wouldCreateCycle(serviceId, dependsOnServiceId, existingEdges)) {
            throw new InvalidRequestException("This dependency would create a cycle.");
        }

        ServiceDependency saved = dependencyRepository.save(new ServiceDependency(service, dependsOnService));
        return ServiceDependencyResponse.of(saved);
    }

    @Transactional
    public void removeDependency(UUID serviceId, UUID dependsOnServiceId) {
        requireService(serviceId);
        if (!dependencyRepository.existsByServiceIdAndDependsOnServiceId(serviceId, dependsOnServiceId)) {
            throw new NotFoundException("Dependency not found.");
        }
        dependencyRepository.deleteByServiceIdAndDependsOnServiceId(serviceId, dependsOnServiceId);
    }

    private ManagedService requireService(UUID serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service " + serviceId + " was not found."));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private Device requireDeviceInProjectIfPresent(UUID projectId, UUID deviceId) {
        if (deviceId == null) {
            return null;
        }
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("Device " + deviceId + " was not found."));
        if (!device.getProject().getId().equals(projectId)) {
            throw new NotFoundException("Device " + deviceId + " does not belong to project " + projectId + ".");
        }
        return device;
    }

    private Map<UUID, List<UUID>> groupDependsOn(List<ServiceDependency> dependencies) {
        return dependencies.stream()
                .collect(Collectors.groupingBy(
                        dependency -> dependency.getService().getId(),
                        Collectors.mapping(dependency -> dependency.getDependsOnService().getId(), Collectors.toList())));
    }

    private boolean matches(ManagedService service, ManagedServiceFilter filter) {
        if (filter.status() != null && service.getStatus() != filter.status()) {
            return false;
        }
        if (filter.runtimeType() != null && service.getRuntimeType() != filter.runtimeType()) {
            return false;
        }
        if (filter.sensitivity() != null && service.getSensitivity() != filter.sensitivity()) {
            return false;
        }
        if (filter.externallyExposed() != null && service.isExternallyExposed() != filter.externallyExposed()) {
            return false;
        }
        return true;
    }
}
