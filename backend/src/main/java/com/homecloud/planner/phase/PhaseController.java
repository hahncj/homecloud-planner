package com.homecloud.planner.phase;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/phases")
class PhaseController {

    private final PhaseService phaseService;

    PhaseController(PhaseService phaseService) {
        this.phaseService = phaseService;
    }

    @GetMapping
    List<PhaseResponse> listPhases(@PathVariable UUID projectId) {
        return phaseService.listPhases(projectId);
    }

    @PostMapping
    ResponseEntity<PhaseResponse> createPhase(@PathVariable UUID projectId, @Valid @RequestBody PhaseRequest request) {
        PhaseResponse created = phaseService.createPhase(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{phaseId}")
    PhaseResponse updatePhase(
            @PathVariable UUID projectId, @PathVariable UUID phaseId, @Valid @RequestBody PhaseRequest request) {
        return phaseService.updatePhase(projectId, phaseId, request);
    }

    @DeleteMapping("/{phaseId}")
    ResponseEntity<Void> deletePhase(@PathVariable UUID projectId, @PathVariable UUID phaseId) {
        phaseService.deletePhase(projectId, phaseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    List<PhaseResponse> reorderPhases(@PathVariable UUID projectId, @Valid @RequestBody PhaseReorderRequest request) {
        return phaseService.reorderPhases(projectId, request);
    }
}
