package com.sanskar.taskmanager.controller;

import com.sanskar.taskmanager.dto.request.AddMemberRequest;
import com.sanskar.taskmanager.dto.request.ProjectRequest;
import com.sanskar.taskmanager.dto.response.ProjectMemberResponse;
import com.sanskar.taskmanager.dto.response.ProjectResponse;
import com.sanskar.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(Authentication authentication) {
        return ResponseEntity.ok(projectService.getMyProjects(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                           Authentication authentication) {
        return ResponseEntity.ok(projectService.createProject(request, authentication.getName()));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.isProjectMember(#projectId, authentication)")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable("projectId") Long projectId,
                                                       Authentication authentication) {
        return ResponseEntity.ok(projectService.getProjectById(projectId, authentication.getName()));
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("@projectSecurity.isProjectMember(#projectId, authentication)")
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(@PathVariable("projectId") Long projectId) {
        return ResponseEntity.ok(projectService.getMembers(projectId));
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("@projectSecurity.isProjectAdmin(#projectId, authentication)")
    public ResponseEntity<ProjectMemberResponse> addMember(@PathVariable("projectId") Long projectId,
                                                            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(projectService.addMember(projectId, request));
    }
}
