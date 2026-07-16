package com.sanskar.taskmanager.service;

import com.sanskar.taskmanager.dto.request.AddMemberRequest;
import com.sanskar.taskmanager.dto.request.ProjectRequest;
import com.sanskar.taskmanager.dto.response.ProjectMemberResponse;
import com.sanskar.taskmanager.dto.response.ProjectResponse;
import com.sanskar.taskmanager.entity.Project;
import com.sanskar.taskmanager.entity.ProjectMember;
import com.sanskar.taskmanager.entity.ProjectRole;
import com.sanskar.taskmanager.entity.User;
import com.sanskar.taskmanager.exception.ResourceNotFoundException;
import com.sanskar.taskmanager.repository.ProjectMemberRepository;
import com.sanskar.taskmanager.repository.ProjectRepository;
import com.sanskar.taskmanager.repository.TaskRepository;
import com.sanskar.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public List<ProjectResponse> getMyProjects(String email) {
        List<ProjectMember> memberships = projectMemberRepository.findByUserEmail(email);
        return memberships.stream()
                .map(pm -> toProjectResponse(pm.getProject(), pm.getRole()))
                .collect(Collectors.toList());
    }

    public ProjectResponse createProject(ProjectRequest request, String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());
        projectRepository.save(project);

        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setProject(project);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ProjectRole.ADMIN);
        projectMemberRepository.save(ownerMembership);

        return toProjectResponse(project, ProjectRole.ADMIN);
    }

    public ProjectResponse getProjectById(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserEmail(projectId, email)
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this project"));

        return toProjectResponse(project, membership.getRole());
    }

    public List<ProjectMemberResponse> getMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(pm -> new ProjectMemberResponse(
                        pm.getUser().getId(),
                        pm.getUser().getName(),
                        pm.getUser().getEmail(),
                        pm.getRole().name()))
                .collect(Collectors.toList());
    }

    public ProjectMemberResponse addMember(Long projectId, AddMemberRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with that email"));

        if (projectMemberRepository.existsByProjectIdAndUserEmail(projectId, request.getEmail())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setRole(request.getRole() != null ? request.getRole() : ProjectRole.MEMBER);
        projectMemberRepository.save(member);

        return new ProjectMemberResponse(user.getId(), user.getName(), user.getEmail(), member.getRole().name());
    }

    private ProjectResponse toProjectResponse(Project project, ProjectRole currentUserRole) {
        int memberCount = projectMemberRepository.findByProjectId(project.getId()).size();
        int taskCount = taskRepository.findByProjectId(project.getId()).size();

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getName(),
                memberCount,
                taskCount,
                project.getCreatedAt(),
                currentUserRole.name()
        );
    }
}
