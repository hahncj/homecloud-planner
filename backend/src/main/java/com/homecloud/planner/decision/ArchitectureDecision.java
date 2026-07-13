package com.homecloud.planner.decision;

import com.homecloud.planner.device.Device;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.servicecatalog.ManagedService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "architecture_decision")
public class ArchitectureDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionStatus status;

    @Column
    private String context;

    @Column(nullable = false)
    private String decision;

    @Column(name = "alternatives_considered")
    private String alternativesConsidered;

    @Column
    private String consequences;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "revisit_criteria")
    private String revisitCriteria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "decision_related_device",
            joinColumns = @JoinColumn(name = "decision_id"),
            inverseJoinColumns = @JoinColumn(name = "device_id"))
    private Set<Device> relatedDevices = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "decision_related_service",
            joinColumns = @JoinColumn(name = "decision_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<ManagedService> relatedServices = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ArchitectureDecision() {
    }

    public ArchitectureDecision(
            Project project,
            String title,
            DecisionStatus status,
            String context,
            String decision,
            String alternativesConsidered,
            String consequences,
            LocalDate decisionDate,
            String revisitCriteria) {
        this.project = project;
        this.title = title;
        this.status = status;
        this.context = context;
        this.decision = decision;
        this.alternativesConsidered = alternativesConsidered;
        this.consequences = consequences;
        this.decisionDate = decisionDate;
        this.revisitCriteria = revisitCriteria;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getAlternativesConsidered() {
        return alternativesConsidered;
    }

    public void setAlternativesConsidered(String alternativesConsidered) {
        this.alternativesConsidered = alternativesConsidered;
    }

    public String getConsequences() {
        return consequences;
    }

    public void setConsequences(String consequences) {
        this.consequences = consequences;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getRevisitCriteria() {
        return revisitCriteria;
    }

    public void setRevisitCriteria(String revisitCriteria) {
        this.revisitCriteria = revisitCriteria;
    }

    public Set<Device> getRelatedDevices() {
        return relatedDevices;
    }

    public void setRelatedDevices(Set<Device> relatedDevices) {
        this.relatedDevices = relatedDevices;
    }

    public Set<ManagedService> getRelatedServices() {
        return relatedServices;
    }

    public void setRelatedServices(Set<ManagedService> relatedServices) {
        this.relatedServices = relatedServices;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
