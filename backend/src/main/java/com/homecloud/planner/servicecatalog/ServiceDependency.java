package com.homecloud.planner.servicecatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "service_dependency")
public class ServiceDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private ManagedService service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depends_on_service_id", nullable = false, updatable = false)
    private ManagedService dependsOnService;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ServiceDependency() {
    }

    public ServiceDependency(ManagedService service, ManagedService dependsOnService) {
        this.service = service;
        this.dependsOnService = dependsOnService;
    }

    public UUID getId() {
        return id;
    }

    public ManagedService getService() {
        return service;
    }

    public ManagedService getDependsOnService() {
        return dependsOnService;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
