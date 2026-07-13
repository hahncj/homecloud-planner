package com.homecloud.planner.servicecatalog;

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
class ManagedServiceController {

    private final ManagedServiceService managedServiceService;

    ManagedServiceController(ManagedServiceService managedServiceService) {
        this.managedServiceService = managedServiceService;
    }

    @GetMapping("/projects/{projectId}/services")
    List<ManagedServiceResponse> listServices(
            @PathVariable UUID projectId,
            @RequestParam(required = false) ManagedServiceStatus status,
            @RequestParam(required = false) RuntimeType runtimeType,
            @RequestParam(required = false) Sensitivity sensitivity,
            @RequestParam(required = false) Boolean externallyExposed) {
        return managedServiceService.listServices(
                projectId, new ManagedServiceFilter(status, runtimeType, sensitivity, externallyExposed));
    }

    @GetMapping("/services/{serviceId}")
    ManagedServiceResponse getService(@PathVariable UUID serviceId) {
        return managedServiceService.getService(serviceId);
    }

    @PostMapping("/projects/{projectId}/services")
    ResponseEntity<ManagedServiceResponse> createService(
            @PathVariable UUID projectId, @Valid @RequestBody ManagedServiceRequest request) {
        ManagedServiceResponse created = managedServiceService.createService(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/services/{serviceId}")
    ManagedServiceResponse updateService(
            @PathVariable UUID serviceId, @Valid @RequestBody ManagedServiceRequest request) {
        return managedServiceService.updateService(serviceId, request);
    }

    @DeleteMapping("/services/{serviceId}")
    ResponseEntity<Void> deleteService(@PathVariable UUID serviceId) {
        managedServiceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services/{serviceId}/dependencies")
    List<ServiceDependencyResponse> listDependencies(@PathVariable UUID serviceId) {
        return managedServiceService.listDependencies(serviceId);
    }

    @PostMapping("/services/{serviceId}/dependencies")
    ResponseEntity<ServiceDependencyResponse> addDependency(
            @PathVariable UUID serviceId, @Valid @RequestBody ServiceDependencyRequest request) {
        ServiceDependencyResponse created = managedServiceService.addDependency(serviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/services/{serviceId}/dependencies/{dependsOnServiceId}")
    ResponseEntity<Void> removeDependency(@PathVariable UUID serviceId, @PathVariable UUID dependsOnServiceId) {
        managedServiceService.removeDependency(serviceId, dependsOnServiceId);
        return ResponseEntity.noContent().build();
    }
}
