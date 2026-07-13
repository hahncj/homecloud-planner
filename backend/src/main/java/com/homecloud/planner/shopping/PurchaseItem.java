package com.homecloud.planner.shopping;

import com.homecloud.planner.phase.Phase;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "purchase_item")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id")
    private Phase phase;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 200)
    private String model;

    @Column
    private String description;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "estimated_unit_price", precision = 12, scale = 2)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "actual_unit_price", precision = 12, scale = 2)
    private BigDecimal actualUnitPrice;

    @Column(length = 200)
    private String vendor;

    @Column(name = "purchase_url", length = 2048)
    private String purchaseUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseStatus status;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "warranty_expiration")
    private LocalDate warrantyExpiration;

    @Column(name = "receipt_reference", length = 500)
    private String receiptReference;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PurchaseItem() {
    }

    public PurchaseItem(
            Project project,
            Phase phase,
            String category,
            String productName,
            String manufacturer,
            String model,
            String description,
            int quantity,
            BigDecimal estimatedUnitPrice,
            BigDecimal actualUnitPrice,
            String vendor,
            String purchaseUrl,
            PurchaseStatus status,
            LocalDate purchaseDate,
            LocalDate deliveryDate,
            LocalDate warrantyExpiration,
            String receiptReference,
            String notes) {
        this.project = project;
        this.phase = phase;
        this.category = category;
        this.productName = productName;
        this.manufacturer = manufacturer;
        this.model = model;
        this.description = description;
        this.quantity = quantity;
        this.estimatedUnitPrice = estimatedUnitPrice;
        this.actualUnitPrice = actualUnitPrice;
        this.vendor = vendor;
        this.purchaseUrl = purchaseUrl;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.deliveryDate = deliveryDate;
        this.warrantyExpiration = warrantyExpiration;
        this.receiptReference = receiptReference;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getEstimatedUnitPrice() {
        return estimatedUnitPrice;
    }

    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) {
        this.estimatedUnitPrice = estimatedUnitPrice;
    }

    public BigDecimal getActualUnitPrice() {
        return actualUnitPrice;
    }

    public void setActualUnitPrice(BigDecimal actualUnitPrice) {
        this.actualUnitPrice = actualUnitPrice;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getWarrantyExpiration() {
        return warrantyExpiration;
    }

    public void setWarrantyExpiration(LocalDate warrantyExpiration) {
        this.warrantyExpiration = warrantyExpiration;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
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

    public BigDecimal getEstimatedTotal() {
        return estimatedUnitPrice == null ? null : estimatedUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getActualTotal() {
        return actualUnitPrice == null ? null : actualUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Cancelled items are excluded from committed spending. */
    public boolean isCommitted() {
        return status != PurchaseStatus.CANCELLED;
    }
}
