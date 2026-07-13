package com.homecloud.planner.task;

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
class TaskController {

    private final TaskService taskService;

    TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    List<TaskResponse> listTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) UUID phaseId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Boolean blocked) {
        return taskService.listTasks(projectId, new TaskFilter(phaseId, status, priority, blocked));
    }

    @GetMapping("/tasks/{taskId}")
    TaskResponse getTask(@PathVariable UUID taskId) {
        return taskService.getTask(taskId);
    }

    @PostMapping("/projects/{projectId}/phases/{phaseId}/tasks")
    ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId, @PathVariable UUID phaseId, @Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.createTask(projectId, phaseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/tasks/{taskId}")
    TaskResponse updateTask(@PathVariable UUID taskId, @Valid @RequestBody TaskRequest request) {
        return taskService.updateTask(taskId, request);
    }

    @DeleteMapping("/tasks/{taskId}")
    ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}/dependencies")
    List<TaskDependencyResponse> listDependencies(@PathVariable UUID taskId) {
        return taskService.listDependencies(taskId);
    }

    @PostMapping("/tasks/{taskId}/dependencies")
    ResponseEntity<TaskDependencyResponse> addDependency(
            @PathVariable UUID taskId, @Valid @RequestBody TaskDependencyRequest request) {
        TaskDependencyResponse created = taskService.addDependency(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/tasks/{taskId}/dependencies/{dependsOnTaskId}")
    ResponseEntity<Void> removeDependency(@PathVariable UUID taskId, @PathVariable UUID dependsOnTaskId) {
        taskService.removeDependency(taskId, dependsOnTaskId);
        return ResponseEntity.noContent().build();
    }
}
