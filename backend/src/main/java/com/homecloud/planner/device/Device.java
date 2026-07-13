package com.homecloud.planner.device;

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
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 200)
    private String model;

    @Column(name = "serial_number", length = 200)
    private String serialNumber;

    @Column(length = 100)
    private String role;

    @Column(length = 200)
    private String location;

    @Column(length = 255)
    private String hostname;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column
    private Integer vlan;

    @Column(name = "operating_system", length = 200)
    private String operatingSystem;

    @Column(name = "firmware_version", length = 100)
    private String firmwareVersion;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "warranty_expiration")
    private LocalDate warrantyExpiration;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    private LifecycleStatus lifecycleStatus;

    @Column(name = "replacement_target")
    private LocalDate replacementTarget;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Device() {
    }

    public Device(
            Project project,
            String name,
            String manufacturer,
            String model,
            String serialNumber,
            String role,
            String location,
            String hostname,
            String ipAddress,
            String macAddress,
            Integer vlan,
            String operatingSystem,
            String firmwareVersion,
            LocalDate purchaseDate,
            LocalDate warrantyExpiration,
            LifecycleStatus lifecycleStatus,
            LocalDate replacementTarget,
            String notes) {
        this.project = project;
        this.name = name;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.role = role;
        this.location = location;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.vlan = vlan;
        this.operatingSystem = operatingSystem;
        this.firmwareVersion = firmwareVersion;
        this.purchaseDate = purchaseDate;
        this.warrantyExpiration = warrantyExpiration;
        this.lifecycleStatus = lifecycleStatus;
        this.replacementTarget = replacementTarget;
        this.notes = notes;
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Integer getVlan() {
        return vlan;
    }

    public void setVlan(Integer vlan) {
        this.vlan = vlan;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getWarrantyExpiration() {
        return warrantyExpiration;
    }

    public void setWarrantyExpiration(LocalDate warrantyExpiration) {
        this.warrantyExpiration = warrantyExpiration;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public LocalDate getReplacementTarget() {
        return replacementTarget;
    }

    public void setReplacementTarget(LocalDate replacementTarget) {
        this.replacementTarget = replacementTarget;
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
