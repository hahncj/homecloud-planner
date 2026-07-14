package com.homecloud.planner.decision;

import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.device.Device;
import com.homecloud.planner.device.DeviceRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.servicecatalog.ManagedService;
import com.homecloud.planner.servicecatalog.ManagedServiceRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchitectureDecisionService {

    private final ArchitectureDecisionRepository decisionRepository;
    private final ProjectRepository projectRepository;
    private final DeviceRepository deviceRepository;
    private final ManagedServiceRepository managedServiceRepository;

    public ArchitectureDecisionService(
            ArchitectureDecisionRepository decisionRepository,
            ProjectRepository projectRepository,
            DeviceRepository deviceRepository,
            ManagedServiceRepository managedServiceRepository) {
        this.decisionRepository = decisionRepository;
        this.projectRepository = projectRepository;
        this.deviceRepository = deviceRepository;
        this.managedServiceRepository = managedServiceRepository;
    }

    @Transactional(readOnly = true)
    public List<ArchitectureDecisionResponse> listDecisions(UUID projectId, ArchitectureDecisionFilter filter) {
        requireProject(projectId);
        return decisionRepository.findByProjectId(projectId).stream()
                .filter(decision -> filter.status() == null || decision.getStatus() == filter.status())
                .map(ArchitectureDecisionResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArchitectureDecisionResponse getDecision(UUID decisionId) {
        return ArchitectureDecisionResponse.of(requireDecision(decisionId));
    }

    @Transactional
    public ArchitectureDecisionResponse createDecision(UUID projectId, ArchitectureDecisionRequest request) {
        Project project = requireProject(projectId);
        ArchitectureDecision decision = new ArchitectureDecision(
                project,
                request.title(),
                request.status(),
                request.context(),
                request.decision(),
                request.alternativesConsidered(),
                request.consequences(),
                request.decisionDate(),
                request.revisitCriteria());
        decision.setRelatedDevices(requireDevicesInProject(projectId, request.relatedDeviceIds()));
        decision.setRelatedServices(requireServicesInProject(projectId, request.relatedServiceIds()));
        return ArchitectureDecisionResponse.of(decisionRepository.save(decision));
    }

    @Transactional
    public ArchitectureDecisionResponse updateDecision(UUID decisionId, ArchitectureDecisionRequest request) {
        ArchitectureDecision decision = requireDecision(decisionId);
        UUID projectId = decision.getProject().getId();
        decision.setTitle(request.title());
        decision.setStatus(request.status());
        decision.setContext(request.context());
        decision.setDecision(request.decision());
        decision.setAlternativesConsidered(request.alternativesConsidered());
        decision.setConsequences(request.consequences());
        decision.setDecisionDate(request.decisionDate());
        decision.setRevisitCriteria(request.revisitCriteria());
        decision.setRelatedDevices(requireDevicesInProject(projectId, request.relatedDeviceIds()));
        decision.setRelatedServices(requireServicesInProject(projectId, request.relatedServiceIds()));
        return ArchitectureDecisionResponse.of(decision);
    }

    @Transactional
    public void deleteDecision(UUID decisionId) {
        decisionRepository.delete(requireDecision(decisionId));
    }

    private ArchitectureDecision requireDecision(UUID decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new NotFoundException("Decision " + decisionId + " was not found."));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private Set<Device> requireDevicesInProject(UUID projectId, List<UUID> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Device> devices = new HashSet<>();
        for (UUID deviceId : deviceIds) {
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new NotFoundException("Device " + deviceId + " was not found."));
            if (!device.getProject().getId().equals(projectId)) {
                throw new NotFoundException("Device " + deviceId + " does not belong to project " + projectId + ".");
            }
            devices.add(device);
        }
        return devices;
    }

    private Set<ManagedService> requireServicesInProject(UUID projectId, List<UUID> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<ManagedService> services = new HashSet<>();
        for (UUID serviceId : serviceIds) {
            ManagedService service = managedServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new NotFoundException("Service " + serviceId + " was not found."));
            if (!service.getProject().getId().equals(projectId)) {
                throw new NotFoundException("Service " + serviceId + " does not belong to project " + projectId + ".");
            }
            services.add(service);
        }
        return services;
    }
}
