package com.homecloud.planner.phase;

import com.homecloud.planner.common.ConflictException;
import com.homecloud.planner.common.InvalidRequestException;
import com.homecloud.planner.common.NotFoundException;
import com.homecloud.planner.project.Project;
import com.homecloud.planner.project.ProjectRepository;
import com.homecloud.planner.roadmap.ProgressSummary;
import com.homecloud.planner.roadmap.TaskProgressCalculator;
import com.homecloud.planner.task.Task;
import com.homecloud.planner.task.TaskDependency;
import com.homecloud.planner.task.TaskDependencyRepository;
import com.homecloud.planner.task.TaskRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhaseService {

    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final TaskProgressCalculator progressCalculator;

    public PhaseService(
            PhaseRepository phaseRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            TaskDependencyRepository dependencyRepository,
            TaskProgressCalculator progressCalculator) {
        this.phaseRepository = phaseRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.dependencyRepository = dependencyRepository;
        this.progressCalculator = progressCalculator;
    }

    @Transactional(readOnly = true)
    public List<PhaseResponse> listPhases(UUID projectId) {
        requireProject(projectId);
        List<Phase> phases = phaseRepository.findByProjectIdOrderBySequenceAsc(projectId);
        Map<UUID, ProgressSummary> progressByPhase = progressByPhase(projectId, phases);
        return phases.stream()
                .map(phase -> PhaseResponse.of(phase, progressByPhase.getOrDefault(phase.getId(), ProgressSummary.EMPTY)))
                .toList();
    }

    @Transactional
    public PhaseResponse createPhase(UUID projectId, PhaseRequest request) {
        Project project = requireProject(projectId);
        int nextSequence = phaseRepository.findByProjectIdOrderBySequenceAsc(projectId).stream()
                .mapToInt(Phase::getSequence)
                .max()
                .orElse(0) + 1;
        Phase phase = new Phase(project, request.name(), request.description(), nextSequence);
        Phase saved = phaseRepository.save(phase);
        return PhaseResponse.of(saved, ProgressSummary.EMPTY);
    }

    @Transactional
    public PhaseResponse updatePhase(UUID projectId, UUID phaseId, PhaseRequest request) {
        Phase phase = requirePhaseInProject(projectId, phaseId);
        phase.setName(request.name());
        phase.setDescription(request.description());
        ProgressSummary progress = progressByPhase(projectId, List.of(phase)).getOrDefault(phaseId, ProgressSummary.EMPTY);
        return PhaseResponse.of(phase, progress);
    }

    @Transactional
    public void deletePhase(UUID projectId, UUID phaseId) {
        Phase phase = requirePhaseInProject(projectId, phaseId);
        if (taskRepository.existsByPhaseId(phaseId)) {
            throw new ConflictException("Phase has tasks; delete or move its tasks before deleting the phase.");
        }
        int removedSequence = phase.getSequence();
        phaseRepository.delete(phase);
        phaseRepository.flush();
        List<Phase> remaining = phaseRepository.findByProjectIdOrderBySequenceAsc(projectId);
        for (Phase remainingPhase : remaining) {
            if (remainingPhase.getSequence() > removedSequence) {
                remainingPhase.setSequence(remainingPhase.getSequence() - 1);
            }
        }
    }

    @Transactional
    public List<PhaseResponse> reorderPhases(UUID projectId, PhaseReorderRequest request) {
        requireProject(projectId);
        List<Phase> phases = phaseRepository.findByProjectIdOrderBySequenceAsc(projectId);
        Set<UUID> existingIds = phases.stream().map(Phase::getId).collect(Collectors.toSet());
        Set<UUID> requestedIds = new HashSet<>(request.orderedPhaseIds());

        if (!existingIds.equals(requestedIds) || requestedIds.size() != request.orderedPhaseIds().size()) {
            throw new InvalidRequestException(
                    "orderedPhaseIds must contain every phase of the project exactly once.");
        }

        Map<UUID, Phase> phaseById = phases.stream().collect(Collectors.toMap(Phase::getId, phase -> phase));
        // Temporarily push sequences past the max to avoid violating the unique
        // (project_id, sequence) constraint while reassigning within the same transaction.
        int offset = phases.size();
        for (Phase phase : phases) {
            phase.setSequence(phase.getSequence() + offset);
        }
        phaseRepository.flush();

        List<UUID> orderedIds = request.orderedPhaseIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            phaseById.get(orderedIds.get(i)).setSequence(i + 1);
        }
        phaseRepository.flush();

        return listPhases(projectId);
    }

    private Map<UUID, ProgressSummary> progressByPhase(UUID projectId, List<Phase> phases) {
        List<Task> allProjectTasks = taskRepository.findByPhaseProjectId(projectId);
        List<TaskDependency> allProjectDependencies = dependencyRepository.findByTaskPhaseProjectId(projectId);
        Map<UUID, Boolean> blockedFlags = progressCalculator.computeBlockedFlags(allProjectTasks, allProjectDependencies);

        Map<UUID, List<Task>> tasksByPhase = allProjectTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getPhase().getId()));

        return phases.stream()
                .collect(Collectors.toMap(
                        Phase::getId,
                        phase -> progressCalculator.summarize(
                                tasksByPhase.getOrDefault(phase.getId(), List.of()), blockedFlags)));
    }

    private Project requireProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project " + projectId + " was not found."));
    }

    private Phase requirePhaseInProject(UUID projectId, UUID phaseId) {
        requireProject(projectId);
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new NotFoundException("Phase " + phaseId + " was not found."));
        if (!phase.getProject().getId().equals(projectId)) {
            throw new NotFoundException("Phase " + phaseId + " does not belong to project " + projectId + ".");
        }
        return phase;
    }
}
