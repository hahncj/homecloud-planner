package com.homecloud.planner.shopping;

import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.phase.Phase;
import com.homecloud.planner.phase.PhaseRepository;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final ProjectRepository projectRepository;
    private final PhaseRepository phaseRepository;

    public PurchaseItemService(
            PurchaseItemRepository purchaseItemRepository,
            ProjectRepository projectRepository,
            PhaseRepository phaseRepository) {
        this.purchaseItemRepository = purchaseItemRepository;
        this.projectRepository = projectRepository;
        this.phaseRepository = phaseRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemResponse> listPurchaseItems(UUID projectId, PurchaseItemFilter filter) {
        requireProject(projectId);
        return purchaseItemRepository.findByProjectId(projectId).stream()
                .filter(item -> matches(item, filter))
                .map(PurchaseItemResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseItemResponse getPurchaseItem(UUID purchaseItemId) {
        return PurchaseItemResponse.of(requirePurchaseItem(purchaseItemId));
    }

    @Transactional
    public PurchaseItemResponse createPurchaseItem(UUID projectId, PurchaseItemRequest request) {
        Project project = requireProject(projectId);
        Phase phase = requirePhaseInProjectIfPresent(projectId, request.phaseId());
        PurchaseItem item = new PurchaseItem(
                project,
                phase,
                request.category(),
                request.productName(),
                request.manufacturer(),
                request.model(),
                request.description(),
                request.quantity(),
                request.estimatedUnitPrice(),
                request.actualUnitPrice(),
                request.vendor(),
                request.purchaseUrl(),
                request.status(),
                request.purchaseDate(),
                request.deliveryDate(),
                request.warrantyExpiration(),
                request.receiptReference(),
                request.notes());
        return PurchaseItemResponse.of(purchaseItemRepository.save(item));
    }

    @Transactional
    public PurchaseItemResponse updatePurchaseItem(UUID purchaseItemId, PurchaseItemRequest request) {
        PurchaseItem item = requirePurchaseItem(purchaseItemId);
        item.setPhase(requirePhaseInProjectIfPresent(item.getProject().getId(), request.phaseId()));
        item.setCategory(request.category());
        item.setProductName(request.productName());
        item.setManufacturer(request.manufacturer());
        item.setModel(request.model());
        item.setDescription(request.description());
        item.setQuantity(request.quantity());
        item.setEstimatedUnitPrice(request.estimatedUnitPrice());
        item.setActualUnitPrice(request.actualUnitPrice());
        item.setVendor(request.vendor());
        item.setPurchaseUrl(request.purchaseUrl());
        item.setStatus(request.status());
        item.setPurchaseDate(request.purchaseDate());
        item.setDeliveryDate(request.deliveryDate());
        item.setWarrantyExpiration(request.warrantyExpiration());
        item.setReceiptReference(request.receiptReference());
        item.setNotes(request.notes());
        return PurchaseItemResponse.of(item);
    }

    @Transactional
    public void deletePurchaseItem(UUID purchaseItemId) {
        purchaseItemRepository.delete(requirePurchaseItem(purchaseItemId));
    }

    private PurchaseItem requirePurchaseItem(UUID purchaseItemId) {
        return purchaseItemRepository.findById(purchaseItemId)
                .orElseThrow(() -> new NotFoundException("Purchase item " + purchaseItemId + " was not found."));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private Phase requirePhaseInProjectIfPresent(UUID projectId, UUID phaseId) {
        if (phaseId == null) {
            return null;
        }
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new NotFoundException("Phase " + phaseId + " was not found."));
        if (!phase.getProject().getId().equals(projectId)) {
            throw new NotFoundException("Phase " + phaseId + " does not belong to project " + projectId + ".");
        }
        return phase;
    }

    private boolean matches(PurchaseItem item, PurchaseItemFilter filter) {
        if (filter.status() != null && item.getStatus() != filter.status()) {
            return false;
        }
        if (filter.category() != null && !filter.category().equalsIgnoreCase(item.getCategory())) {
            return false;
        }
        if (filter.phaseId() != null
                && (item.getPhase() == null || !item.getPhase().getId().equals(filter.phaseId()))) {
            return false;
        }
        return true;
    }
}
