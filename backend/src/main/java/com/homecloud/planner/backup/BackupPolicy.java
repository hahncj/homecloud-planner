package com.homecloud.planner.backup;

import com.homecloud.planner.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "backup_policy")
public class BackupPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "data_category", nullable = false, length = 200)
    private String dataCategory;

    @Column(name = "primary_location", nullable = false, length = 200)
    private String primaryLocation;

    @Column(name = "local_backup_location", length = 200)
    private String localBackupLocation;

    @Column(name = "offsite_backup_location", length = 200)
    private String offsiteBackupLocation;

    @Column(nullable = false)
    private boolean encrypted;

    @Column(name = "contains_sensitive_data", nullable = false)
    private boolean containsSensitiveData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BackupFrequency frequency;

    @Column(length = 200)
    private String retention;

    @Column(name = "recovery_point_objective", length = 100)
    private String recoveryPointObjective;

    @Column(name = "recovery_time_objective", length = 100)
    private String recoveryTimeObjective;

    @Column(name = "last_verified_date")
    private LocalDate lastVerifiedDate;

    @Column(name = "verification_notes")
    private String verificationNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BackupPolicy() {
    }

    public BackupPolicy(
            Project project,
            String name,
            String dataCategory,
            String primaryLocation,
            String localBackupLocation,
            String offsiteBackupLocation,
            boolean encrypted,
            boolean containsSensitiveData,
            BackupFrequency frequency,
            String retention,
            String recoveryPointObjective,
            String recoveryTimeObjective,
            LocalDate lastVerifiedDate,
            String verificationNotes) {
        this.project = project;
        this.name = name;
        this.dataCategory = dataCategory;
        this.primaryLocation = primaryLocation;
        this.localBackupLocation = localBackupLocation;
        this.offsiteBackupLocation = offsiteBackupLocation;
        this.encrypted = encrypted;
        this.containsSensitiveData = containsSensitiveData;
        this.frequency = frequency;
        this.retention = retention;
        this.recoveryPointObjective = recoveryPointObjective;
        this.recoveryTimeObjective = recoveryTimeObjective;
        this.lastVerifiedDate = lastVerifiedDate;
        this.verificationNotes = verificationNotes;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public String getPrimaryLocation() {
        return primaryLocation;
    }

    public void setPrimaryLocation(String primaryLocation) {
        this.primaryLocation = primaryLocation;
    }

    public String getLocalBackupLocation() {
        return localBackupLocation;
    }

    public void setLocalBackupLocation(String localBackupLocation) {
        this.localBackupLocation = localBackupLocation;
    }

    public String getOffsiteBackupLocation() {
        return offsiteBackupLocation;
    }

    public void setOffsiteBackupLocation(String offsiteBackupLocation) {
        this.offsiteBackupLocation = offsiteBackupLocation;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isContainsSensitiveData() {
        return containsSensitiveData;
    }

    public void setContainsSensitiveData(boolean containsSensitiveData) {
        this.containsSensitiveData = containsSensitiveData;
    }

    public BackupFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(BackupFrequency frequency) {
        this.frequency = frequency;
    }

    public String getRetention() {
        return retention;
    }

    public void setRetention(String retention) {
        this.retention = retention;
    }

    public String getRecoveryPointObjective() {
        return recoveryPointObjective;
    }

    public void setRecoveryPointObjective(String recoveryPointObjective) {
        this.recoveryPointObjective = recoveryPointObjective;
    }

    public String getRecoveryTimeObjective() {
        return recoveryTimeObjective;
    }

    public void setRecoveryTimeObjective(String recoveryTimeObjective) {
        this.recoveryTimeObjective = recoveryTimeObjective;
    }

    public LocalDate getLastVerifiedDate() {
        return lastVerifiedDate;
    }

    public void setLastVerifiedDate(LocalDate lastVerifiedDate) {
        this.lastVerifiedDate = lastVerifiedDate;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
