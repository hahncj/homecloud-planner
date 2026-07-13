package com.homecloud.planner.decision;

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
class ArchitectureDecisionController {

    private final ArchitectureDecisionService decisionService;

    ArchitectureDecisionController(ArchitectureDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @GetMapping("/projects/{projectId}/decisions")
    List<ArchitectureDecisionResponse> listDecisions(
            @PathVariable UUID projectId, @RequestParam(required = false) DecisionStatus status) {
        return decisionService.listDecisions(projectId, new ArchitectureDecisionFilter(status));
    }

    @GetMapping("/decisions/{decisionId}")
    ArchitectureDecisionResponse getDecision(@PathVariable UUID decisionId) {
        return decisionService.getDecision(decisionId);
    }

    @PostMapping("/projects/{projectId}/decisions")
    ResponseEntity<ArchitectureDecisionResponse> createDecision(
            @PathVariable UUID projectId, @Valid @RequestBody ArchitectureDecisionRequest request) {
        ArchitectureDecisionResponse created = decisionService.createDecision(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/decisions/{decisionId}")
    ArchitectureDecisionResponse updateDecision(
            @PathVariable UUID decisionId, @Valid @RequestBody ArchitectureDecisionRequest request) {
        return decisionService.updateDecision(decisionId, request);
    }

    @DeleteMapping("/decisions/{decisionId}")
    ResponseEntity<Void> deleteDecision(@PathVariable UUID decisionId) {
        decisionService.deleteDecision(decisionId);
        return ResponseEntity.noContent().build();
    }
}
