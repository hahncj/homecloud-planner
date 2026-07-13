package com.homecloud.planner.device;

import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ProjectRepository projectRepository;

    public DeviceService(DeviceRepository deviceRepository, ProjectRepository projectRepository) {
        this.deviceRepository = deviceRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevices(UUID projectId, DeviceFilter filter) {
        requireProject(projectId);
        return deviceRepository.findByProjectId(projectId).stream()
                .filter(device -> matches(device, filter))
                .map(DeviceResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse getDevice(UUID deviceId) {
        return DeviceResponse.of(requireDevice(deviceId));
    }

    @Transactional
    public DeviceResponse createDevice(UUID projectId, DeviceRequest request) {
        Project project = requireProject(projectId);
        Device device = new Device(
                project,
                request.name(),
                request.manufacturer(),
                request.model(),
                request.serialNumber(),
                request.role(),
                request.location(),
                request.hostname(),
                request.ipAddress(),
                request.macAddress(),
                request.vlan(),
                request.operatingSystem(),
                request.firmwareVersion(),
                request.purchaseDate(),
                request.warrantyExpiration(),
                request.lifecycleStatus(),
                request.replacementTarget(),
                request.notes());
        return DeviceResponse.of(deviceRepository.save(device));
    }

    @Transactional
    public DeviceResponse updateDevice(UUID deviceId, DeviceRequest request) {
        Device device = requireDevice(deviceId);
        device.setName(request.name());
        device.setManufacturer(request.manufacturer());
        device.setModel(request.model());
        device.setSerialNumber(request.serialNumber());
        device.setRole(request.role());
        device.setLocation(request.location());
        device.setHostname(request.hostname());
        device.setIpAddress(request.ipAddress());
        device.setMacAddress(request.macAddress());
        device.setVlan(request.vlan());
        device.setOperatingSystem(request.operatingSystem());
        device.setFirmwareVersion(request.firmwareVersion());
        device.setPurchaseDate(request.purchaseDate());
        device.setWarrantyExpiration(request.warrantyExpiration());
        device.setLifecycleStatus(request.lifecycleStatus());
        device.setReplacementTarget(request.replacementTarget());
        device.setNotes(request.notes());
        return DeviceResponse.of(device);
    }

    @Transactional
    public void deleteDevice(UUID deviceId) {
        deviceRepository.delete(requireDevice(deviceId));
    }

    private Device requireDevice(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new NotFoundException("Device " + deviceId + " was not found."));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private boolean matches(Device device, DeviceFilter filter) {
        if (filter.lifecycleStatus() != null && device.getLifecycleStatus() != filter.lifecycleStatus()) {
            return false;
        }
        if (filter.role() != null && !filter.role().equalsIgnoreCase(device.getRole())) {
            return false;
        }
        if (filter.location() != null && !filter.location().equalsIgnoreCase(device.getLocation())) {
            return false;
        }
        return true;
    }
}
