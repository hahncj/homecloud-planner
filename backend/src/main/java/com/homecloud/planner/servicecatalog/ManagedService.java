package com.homecloud.planner.servicecatalog;

import com.homecloud.planner.device.Device;
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
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "managed_service")
public class ManagedService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_device_id")
    private Device hostDevice;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String purpose;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ManagedServiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "runtime_type", nullable = false, length = 20)
    private RuntimeType runtimeType;

    @Column(name = "storage_location", length = 200)
    private String storageLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sensitivity sensitivity;

    @Column(name = "externally_exposed", nullable = false)
    private boolean externallyExposed;

    @Column(name = "authentication_method", length = 200)
    private String authenticationMethod;

    @Column(name = "backup_policy", length = 500)
    private String backupPolicy;

    @Column(name = "documentation_url", length = 2048)
    private String documentationUrl;

    @Column(name = "repository_url", length = 2048)
    private String repositoryUrl;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ManagedService() {
    }

    public ManagedService(
            Project project,
            Device hostDevice,
            String name,
            String purpose,
            String description,
            ManagedServiceStatus status,
            RuntimeType runtimeType,
            String storageLocation,
            Sensitivity sensitivity,
            boolean externallyExposed,
            String authenticationMethod,
            String backupPolicy,
            String documentationUrl,
            String repositoryUrl,
            String notes) {
        this.project = project;
        this.hostDevice = hostDevice;
        this.name = name;
        this.purpose = purpose;
        this.description = description;
        this.status = status;
        this.runtimeType = runtimeType;
        this.storageLocation = storageLocation;
        this.sensitivity = sensitivity;
        this.externallyExposed = externallyExposed;
        this.authenticationMethod = authenticationMethod;
        this.backupPolicy = backupPolicy;
        this.documentationUrl = documentationUrl;
        this.repositoryUrl = repositoryUrl;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public Device getHostDevice() {
        return hostDevice;
    }

    public void setHostDevice(Device hostDevice) {
        this.hostDevice = hostDevice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ManagedServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ManagedServiceStatus status) {
        this.status = status;
    }

    public RuntimeType getRuntimeType() {
        return runtimeType;
    }

    public void setRuntimeType(RuntimeType runtimeType) {
        this.runtimeType = runtimeType;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Sensitivity getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public boolean isExternallyExposed() {
        return externallyExposed;
    }

    public void setExternallyExposed(boolean externallyExposed) {
        this.externallyExposed = externallyExposed;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getBackupPolicy() {
        return backupPolicy;
    }

    public void setBackupPolicy(String backupPolicy) {
        this.backupPolicy = backupPolicy;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
