package com.sanskar.taskmanager.controller;

import com.sanskar.taskmanager.dto.request.StatusUpdateRequest;
import com.sanskar.taskmanager.dto.request.TaskRequest;
import com.sanskar.taskmanager.dto.response.TaskResponse;
import com.sanskar.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("@projectSecurity.isProjectMember(#projectId, authentication)")
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable("projectId") Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId, authentication)")
    public ResponseEntity<TaskResponse> createTask(@PathVariable("projectId") Long projectId,
                                                    @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(projectId, request));
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("@projectSecurity.isProjectMember(#projectId, authentication)")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable("projectId") Long projectId,
                                                      @PathVariable("taskId") Long taskId,
                                                      @Valid @RequestBody StatusUpdateRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.ok(taskService.updateStatus(projectId, taskId, request, authentication.getName()));
    }
}
