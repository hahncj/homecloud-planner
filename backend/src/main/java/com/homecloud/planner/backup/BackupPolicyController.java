package com.homecloud.planner.backup;

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
class BackupPolicyController {

    private final BackupPolicyService backupPolicyService;

    BackupPolicyController(BackupPolicyService backupPolicyService) {
        this.backupPolicyService = backupPolicyService;
    }

    @GetMapping("/projects/{projectId}/backup-policies")
    List<BackupPolicyResponse> listBackupPolicies(
            @PathVariable UUID projectId,
            @RequestParam(required = false) BackupCoverageState coverageState,
            @RequestParam(required = false) Boolean verificationOverdue) {
        return backupPolicyService.listBackupPolicies(projectId, new BackupPolicyFilter(coverageState, verificationOverdue));
    }

    @GetMapping("/backup-policies/{backupPolicyId}")
    BackupPolicyResponse getBackupPolicy(@PathVariable UUID backupPolicyId) {
        return backupPolicyService.getBackupPolicy(backupPolicyId);
    }

    @PostMapping("/projects/{projectId}/backup-policies")
    ResponseEntity<BackupPolicyResponse> createBackupPolicy(
            @PathVariable UUID projectId, @Valid @RequestBody BackupPolicyRequest request) {
        BackupPolicyResponse created = backupPolicyService.createBackupPolicy(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/backup-policies/{backupPolicyId}")
    BackupPolicyResponse updateBackupPolicy(
            @PathVariable UUID backupPolicyId, @Valid @RequestBody BackupPolicyRequest request) {
        return backupPolicyService.updateBackupPolicy(backupPolicyId, request);
    }

    @DeleteMapping("/backup-policies/{backupPolicyId}")
    ResponseEntity<Void> deleteBackupPolicy(@PathVariable UUID backupPolicyId) {
        backupPolicyService.deleteBackupPolicy(backupPolicyId);
        return ResponseEntity.noContent().build();
    }
}
