package com.homecloud.planner.device;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class DeviceController {

    private final DeviceService deviceService;

    DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/projects/{projectId}/devices")
    List<DeviceResponse> listDevices(
            @PathVariable UUID projectId,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location) {
        return deviceService.listDevices(projectId, new DeviceFilter(lifecycleStatus, role, location));
    }

    @GetMapping("/devices/{deviceId}")
    DeviceResponse getDevice(@PathVariable UUID deviceId) {
        return deviceService.getDevice(deviceId);
    }

    @PostMapping("/projects/{projectId}/devices")
    ResponseEntity<DeviceResponse> createDevice(
            @PathVariable UUID projectId, @Valid @RequestBody DeviceRequest request) {
        DeviceResponse created = deviceService.createDevice(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/devices/{deviceId}")
    DeviceResponse updateDevice(@PathVariable UUID deviceId, @Valid @RequestBody DeviceRequest request) {
        return deviceService.updateDevice(deviceId, request);
    }

    @DeleteMapping("/devices/{deviceId}")
    ResponseEntity<Void> deleteDevice(@PathVariable UUID deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
