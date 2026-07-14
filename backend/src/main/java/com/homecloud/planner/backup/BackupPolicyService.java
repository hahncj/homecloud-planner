package com.homecloud.planner.backup;

import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BackupPolicyService {

    private final BackupPolicyRepository backupPolicyRepository;
    private final ProjectRepository projectRepository;
    private final BackupCoverageCalculator coverageCalculator;

    public BackupPolicyService(
            BackupPolicyRepository backupPolicyRepository,
            ProjectRepository projectRepository,
            BackupCoverageCalculator coverageCalculator) {
        this.backupPolicyRepository = backupPolicyRepository;
        this.projectRepository = projectRepository;
        this.coverageCalculator = coverageCalculator;
    }

    @Transactional(readOnly = true)
    public List<BackupPolicyResponse> listBackupPolicies(UUID projectId, BackupPolicyFilter filter) {
        requireProject(projectId);
        return backupPolicyRepository.findByProjectId(projectId).stream()
                .map(policy -> BackupPolicyResponse.of(policy, coverageCalculator))
                .filter(response -> matches(response, filter))
                .toList();
    }

    @Transactional(readOnly = true)
    public BackupPolicyResponse getBackupPolicy(UUID backupPolicyId) {
        return BackupPolicyResponse.of(requireBackupPolicy(backupPolicyId), coverageCalculator);
    }

    @Transactional
    public BackupPolicyResponse createBackupPolicy(UUID projectId, BackupPolicyRequest request) {
        Project project = requireProject(projectId);
        BackupPolicy policy = new BackupPolicy(
                project,
                request.name(),
                request.dataCategory(),
                request.primaryLocation(),
                request.localBackupLocation(),
                request.offsiteBackupLocation(),
                request.encrypted(),
                request.containsSensitiveData(),
                request.frequency(),
                request.retention(),
                request.recoveryPointObjective(),
                request.recoveryTimeObjective(),
                request.lastVerifiedDate(),
                request.verificationNotes());
        return BackupPolicyResponse.of(backupPolicyRepository.save(policy), coverageCalculator);
    }

    @Transactional
    public BackupPolicyResponse updateBackupPolicy(UUID backupPolicyId, BackupPolicyRequest request) {
        BackupPolicy policy = requireBackupPolicy(backupPolicyId);
        policy.setName(request.name());
        policy.setDataCategory(request.dataCategory());
        policy.setPrimaryLocation(request.primaryLocation());
        policy.setLocalBackupLocation(request.localBackupLocation());
        policy.setOffsiteBackupLocation(request.offsiteBackupLocation());
        policy.setEncrypted(request.encrypted());
        policy.setContainsSensitiveData(request.containsSensitiveData());
        policy.setFrequency(request.frequency());
        policy.setRetention(request.retention());
        policy.setRecoveryPointObjective(request.recoveryPointObjective());
        policy.setRecoveryTimeObjective(request.recoveryTimeObjective());
        policy.setLastVerifiedDate(request.lastVerifiedDate());
        policy.setVerificationNotes(request.verificationNotes());
        return BackupPolicyResponse.of(policy, coverageCalculator);
    }

    @Transactional
    public void deleteBackupPolicy(UUID backupPolicyId) {
        backupPolicyRepository.delete(requireBackupPolicy(backupPolicyId));
    }

    private BackupPolicy requireBackupPolicy(UUID backupPolicyId) {
        return backupPolicyRepository.findById(backupPolicyId)
                .orElseThrow(() -> new NotFoundException("Backup policy " + backupPolicyId + " was not found."));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private boolean matches(BackupPolicyResponse response, BackupPolicyFilter filter) {
        if (filter.coverageState() != null && response.coverageState() != filter.coverageState()) {
            return false;
        }
        if (filter.verificationOverdue() != null && response.verificationOverdue() != filter.verificationOverdue()) {
            return false;
        }
        return true;
    }
}
