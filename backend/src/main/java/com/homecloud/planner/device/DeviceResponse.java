package com.homecloud.planner.device;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        UUID projectId,
        String name,
        String manufacturer,
        String model,
        String serialNumber,
        String role,
        String location,
        String hostname,
        String ipAddress,
        String macAddress,
        Integer vlan,
        String operatingSystem,
        String firmwareVersion,
        LocalDate purchaseDate,
        LocalDate warrantyExpiration,
        LifecycleStatus lifecycleStatus,
        LocalDate replacementTarget,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public static DeviceResponse of(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getProject().getId(),
                device.getName(),
                device.getManufacturer(),
                device.getModel(),
                device.getSerialNumber(),
                device.getRole(),
                device.getLocation(),
                device.getHostname(),
                device.getIpAddress(),
                device.getMacAddress(),
                device.getVlan(),
                device.getOperatingSystem(),
                device.getFirmwareVersion(),
                device.getPurchaseDate(),
                device.getWarrantyExpiration(),
                device.getLifecycleStatus(),
                device.getReplacementTarget(),
                device.getNotes(),
                device.getCreatedAt(),
                device.getUpdatedAt());
    }
}
