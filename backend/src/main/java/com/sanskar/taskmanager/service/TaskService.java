package com.sanskar.taskmanager.service;

import com.sanskar.taskmanager.dto.request.StatusUpdateRequest;
import com.sanskar.taskmanager.dto.request.TaskRequest;
import com.sanskar.taskmanager.dto.response.TaskResponse;
import com.sanskar.taskmanager.entity.Project;
import com.sanskar.taskmanager.entity.ProjectRole;
import com.sanskar.taskmanager.entity.Task;
import com.sanskar.taskmanager.entity.TaskStatus;
import com.sanskar.taskmanager.entity.User;
import com.sanskar.taskmanager.exception.ResourceNotFoundException;
import com.sanskar.taskmanager.exception.UnauthorizedException;
import com.sanskar.taskmanager.repository.ProjectMemberRepository;
import com.sanskar.taskmanager.repository.ProjectRepository;
import com.sanskar.taskmanager.repository.TaskRepository;
import com.sanskar.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<TaskResponse> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse createTask(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User assignee = userRepository.findByEmail(request.getAssigneeEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));

        boolean assigneeInProject = projectMemberRepository
                .existsByProjectIdAndUserEmail(projectId, request.getAssigneeEmail());
        if (!assigneeInProject) {
            throw new IllegalArgumentException("Assignee must be a member of this project");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(request.getDueDate());
        task.setAssignee(assignee);
        task.setProject(project);

        taskRepository.save(task);
        return toTaskResponse(task);
    }

    public TaskResponse updateStatus(Long projectId, Long taskId, StatusUpdateRequest request, String currentUserEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        boolean isAssignee = task.getAssignee().getEmail().equalsIgnoreCase(currentUserEmail);
        boolean isAdmin = projectMemberRepository.findByProjectIdAndUserEmail(projectId, currentUserEmail)
                .map(pm -> pm.getRole() == ProjectRole.ADMIN)
                .orElse(false);

        if (!isAssignee && !isAdmin) {
            throw new UnauthorizedException("Only the assignee or a project admin can update this task's status");
        }

        task.setStatus(request.getStatus());
        taskRepository.save(task);
        return toTaskResponse(task);
    }

    private TaskResponse toTaskResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getDueDate(),
                task.getAssignee().getName(),
                task.getAssignee().getEmail(),
                task.getProject().getId(),
                overdue
        );
    }
}
